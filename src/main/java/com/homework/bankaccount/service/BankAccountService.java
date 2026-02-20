package com.homework.bankaccount.service;

import com.homework.bankaccount.entities.BalanceEntity;
import com.homework.bankaccount.entities.BankAccountEntity;
import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.exception.BadRequestException;
import com.homework.bankaccount.exception.ExternalSystemUnavailableException;
import com.homework.bankaccount.exception.InsufficientFundsException;
import com.homework.bankaccount.exception.NotFoundException;
import com.homework.bankaccount.httpclient.ExternalSystemRestClient;
import com.homework.bankaccount.httpclient.response.ExternalSystemResponse;
import com.homework.bankaccount.mapper.BalanceMapper;
import com.homework.bankaccount.repository.BalanceRepository;
import com.homework.bankaccount.repository.BankAccountRepository;
import com.homework.bankaccount.request.MoneyRequest;
import com.homework.bankaccount.response.BalanceResponse;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankAccountService {

  private static final Map<Currency, BigDecimal> TO_EUR =
      Map.of(
          Currency.EUR, BigDecimal.ONE,
          Currency.USD, new BigDecimal("0.85"),
          Currency.SEK, new BigDecimal("0.094"),
          Currency.GBP, new BigDecimal("1.15"));
  private final BankAccountRepository bankAccountRepository;
  private final BalanceRepository balanceRepository;
  private final ExternalSystemRestClient externalSystemRestClient;
  private final BalanceMapper balanceMapper;

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
  }

  @Transactional
  public void debitMoney(Long bankAccountId, MoneyRequest request) {
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

    BigDecimal eur = toEur(amount, fromCurrency);
    BigDecimal target = fromEur(eur, toCurrency).setScale(4, RoundingMode.HALF_UP);

    toBalance.setAmount(toBalance.getAmount().add(target).setScale(4, RoundingMode.HALF_UP));
    balanceRepository.saveAll(List.of(fromBalance, toBalance));
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

  private BigDecimal toEur(BigDecimal amount, Currency from) {
    BigDecimal rate =
        Optional.ofNullable(TO_EUR.get(from))
            .orElseThrow(() -> new BadRequestException("Unsupported currency: " + from));
    return amount.multiply(rate);
  }

  private BigDecimal fromEur(BigDecimal amountEur, Currency to) {
    BigDecimal toRateEur =
        Optional.ofNullable(TO_EUR.get(to))
            .orElseThrow(() -> new BadRequestException("Unsupported currency: " + to));
    return amountEur.divide(toRateEur, 8, RoundingMode.HALF_UP);
  }
}
