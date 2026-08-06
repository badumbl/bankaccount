package com.homework.bankaccount.service;

import com.homework.bankaccount.entities.BalanceEntity;
import com.homework.bankaccount.entities.BankAccountEntity;
import com.homework.bankaccount.entities.TransactionEntity;
import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.enums.TransactionType;
import com.homework.bankaccount.exception.ExternalSystemUnavailableException;
import com.homework.bankaccount.exception.InsufficientFundsException;
import com.homework.bankaccount.exception.NotFoundException;
import com.homework.bankaccount.httpclient.ExternalSystemRestClient;
import com.homework.bankaccount.httpclient.response.ExternalSystemResponse;
import com.homework.bankaccount.mapper.BalanceMapper;
import com.homework.bankaccount.mapper.TransactionMapper;
import com.homework.bankaccount.repository.BalanceRepository;
import com.homework.bankaccount.repository.BankAccountRepository;
import com.homework.bankaccount.repository.TransactionRepository;
import com.homework.bankaccount.request.MoneyRequest;
import com.homework.bankaccount.response.BalanceResponse;
import com.homework.bankaccount.response.TransactionResponse;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankAccountService {

  private final BankAccountRepository bankAccountRepository;
  private final BalanceRepository balanceRepository;
  private final TransactionRepository transactionRepository;
  private final ExternalSystemRestClient externalSystemRestClient;
  private final BalanceMapper balanceMapper;
  private final CurrencyRateService currencyRateService;
  private final TransactionMapper transactionMapper;

  private BankAccountEntity getBankAccount(Long bankAccountId) {
    return bankAccountRepository
        .findById(bankAccountId)
        .orElseThrow(() -> new NotFoundException("Bank account not found: " + bankAccountId));
  }

  public BankAccountEntity createAccount(String name) {
    BankAccountEntity bankAccountEntity = new BankAccountEntity();
    bankAccountEntity.setName(name);
    return bankAccountRepository.save(bankAccountEntity);
  }

  @Transactional
  public void addMoney(Long bankAccountId, MoneyRequest request) {
    BankAccountEntity bankAccountEntity = getBankAccount(bankAccountId);

    BalanceEntity balanceEntity =
        bankAccountEntity.getBalances().stream()
            .filter(balance -> balance.getCurrency().equals(request.currency()))
            .findFirst()
            .orElseGet(
                () -> {
                  BalanceEntity balance = new BalanceEntity();
                  balance.setCurrency(request.currency());
                  balance.setBankAccount(bankAccountEntity);
                  balance.setAmount(BigDecimal.ZERO);
                  return balance;
                });

    balanceEntity.setAmount(
        balanceEntity.getAmount().add(request.amount()).setScale(4, RoundingMode.HALF_UP));
    balanceRepository.save(balanceEntity);

    saveTransaction(
        bankAccountEntity,
        TransactionType.DEPOSIT,
        request.amount(),
        request.currency(),
        null,
        null);
  }

  @Transactional
  public void withdrawMoney(Long bankAccountId, MoneyRequest request) {
    BankAccountEntity bankAccountEntity = getBankAccount(bankAccountId);

    BalanceEntity balanceEntity =
        bankAccountEntity.getBalances().stream()
            .filter(balance -> balance.getCurrency().equals(request.currency()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Currency not found: " + request.currency()));

    if (balanceEntity.getAmount().compareTo(request.amount()) < 0) {
      throw new InsufficientFundsException("Insufficient funds for debit");
    }

    ExternalSystemResponse externalSystemResponse =
        externalSystemRestClient.getExternalSystemResponse();
    if (externalSystemResponse.getCode() != 200) {
      throw new ExternalSystemUnavailableException(externalSystemResponse.getDescription());
    }

    balanceEntity.setAmount(
        balanceEntity.getAmount().subtract(request.amount()).setScale(4, RoundingMode.HALF_UP));
    balanceRepository.save(balanceEntity);

    saveTransaction(
        bankAccountEntity,
        TransactionType.WITHDRAWAL,
        request.amount(),
        request.currency(),
        null,
        null);
  }

  public List<BalanceResponse> getBalance(Long bankAccountId) {
    BankAccountEntity bankAccountEntity = getBankAccount(bankAccountId);
    return bankAccountEntity.getBalances().stream().map(balanceMapper::toResponse).toList();
  }

  @Transactional
  public void exchangeCurrency(
      Long bankAccountId, Currency fromCurrency, Currency toCurrency, BigDecimal amount) {
    if (fromCurrency == toCurrency) {
      return;
    }

    BankAccountEntity bankAccountEntity = getBankAccount(bankAccountId);

    Map<Currency, BalanceEntity> balancesMap =
        bankAccountEntity.getBalances().stream()
            .collect(Collectors.toMap(BalanceEntity::getCurrency, Function.identity()));

    BalanceEntity fromBalance = balancesMap.get(fromCurrency);
    BalanceEntity toBalance = getOrCreateBalance(bankAccountEntity, balancesMap, toCurrency);

    if (fromBalance.getAmount().compareTo(amount) < 0) {
      throw new InsufficientFundsException("Insufficient funds for exchange");
    }

    fromBalance.setAmount(
        fromBalance.getAmount().subtract(amount).setScale(4, RoundingMode.HALF_UP));

    BigDecimal rate = currencyRateService.getRate(fromCurrency, toCurrency);
    BigDecimal target = amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);

    toBalance.setAmount(toBalance.getAmount().add(target).setScale(4, RoundingMode.HALF_UP));
    balanceRepository.saveAll(List.of(fromBalance, toBalance));

    saveTransaction(
        bankAccountEntity, TransactionType.EXCHANGE, amount, fromCurrency, target, toCurrency);
  }

  public List<TransactionResponse> getTransactions(Long bankAccountId) {
    // check if a bank account exists
    getBankAccount(bankAccountId);
    return transactionRepository.findByBankAccountIdOrderByCreatedAtDesc(bankAccountId).stream()
        .map(transactionMapper::toResponse)
        .toList();
  }

  private void saveTransaction(
      BankAccountEntity bankAccountEntity,
      TransactionType type,
      BigDecimal amount,
      Currency currency,
      BigDecimal targetAmount,
      Currency targetCurrency) {
    TransactionEntity transactionEntity = new TransactionEntity();
    transactionEntity.setBankAccount(bankAccountEntity);
    transactionEntity.setType(type);
    transactionEntity.setAmount(amount);
    transactionEntity.setCurrency(currency);
    transactionEntity.setTargetAmount(targetAmount);
    transactionEntity.setTargetCurrency(targetCurrency);
    transactionRepository.save(transactionEntity);
  }

  private static BalanceEntity getOrCreateBalance(
      BankAccountEntity account, Map<Currency, BalanceEntity> balances, Currency currency) {

    BalanceEntity existing = balances.get(currency);
    if (existing != null) {
      return existing;
    }

    BalanceEntity created = new BalanceEntity();
    created.setCurrency(currency);
    created.setBankAccount(account);
    account.getBalances().add(created);
    balances.put(currency, created);
    return created;
  }
}
