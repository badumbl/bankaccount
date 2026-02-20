package com.homework.bankaccount.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.homework.bankaccount.entities.BalanceEntity;
import com.homework.bankaccount.entities.BankAccountEntity;
import com.homework.bankaccount.enums.Currency;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {
  @Mock private BankAccountRepository bankAccountRepository;
  @Mock private BalanceRepository balanceRepository;
  @Mock private ExternalSystemRestClient externalSystemRestClient;
  @Mock private BalanceMapper balanceMapper;
  @InjectMocks private BankAccountService bankAccountService;

  private BankAccountEntity bankAccountEntity;

  @BeforeEach
  void setUp() {
    bankAccountEntity = new BankAccountEntity();
    bankAccountEntity.setId(1L);
    bankAccountEntity.setName("Test Account");
  }

  @Test
  void shouldCreateAccount() {
    when(bankAccountRepository.save(any(BankAccountEntity.class))).thenReturn(bankAccountEntity);

    BankAccountEntity created = bankAccountService.createAccount("Test Account");

    assertNotNull(created);
    assertEquals("Test Account", created.getName());
    verify(bankAccountRepository).save(any(BankAccountEntity.class));
  }

  @Test
  void shouldAddMoneyAndCreateBalance() {
    MoneyRequest moneyRequest = new MoneyRequest();
    moneyRequest.setAmount(new BigDecimal("100"));
    moneyRequest.setCurrency(Currency.EUR);

    when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccountEntity));
    bankAccountService.addMoney(1L, moneyRequest);

    ArgumentCaptor<BalanceEntity> balanceCaptor = ArgumentCaptor.forClass(BalanceEntity.class);
    verify(balanceRepository).save(balanceCaptor.capture());

    BalanceEntity savedBalance = balanceCaptor.getValue();
    assertEquals(Currency.EUR, savedBalance.getCurrency());
    assertEquals(new BigDecimal("100.0000"), savedBalance.getAmount());
    assertEquals(bankAccountEntity, savedBalance.getBankAccount());
  }

  @Test
  void shouldAddMoneyToExistingBalance() {
    MoneyRequest moneyRequest = new MoneyRequest();
    moneyRequest.setAmount(new BigDecimal("100"));
    moneyRequest.setCurrency(Currency.EUR);

    BalanceEntity balance = new BalanceEntity();
    balance.setCurrency(Currency.EUR);
    balance.setAmount(new BigDecimal("50"));
    bankAccountEntity.getBalances().add(balance);

    when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccountEntity));
    bankAccountService.addMoney(1L, moneyRequest);

    assertEquals(new BigDecimal("150.0000"), balance.getAmount());
    verify(balanceRepository).save(balance);
  }

  @Test
  void addMoneyShouldThrowNotFoundWhenAccountDoesNotExist() {
    MoneyRequest moneyRequest = new MoneyRequest();
    when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> bankAccountService.addMoney(1L, moneyRequest));
    verify(balanceRepository, never()).save(any());
  }

  @Test
  void shouldDebitMoney() {
    MoneyRequest request = new MoneyRequest();
    request.setAmount(new BigDecimal("50"));
    request.setCurrency(Currency.EUR);

    BalanceEntity balance = new BalanceEntity();
    balance.setCurrency(Currency.EUR);
    balance.setAmount(new BigDecimal("100"));
    bankAccountEntity.getBalances().add(balance);

    ExternalSystemResponse response = new ExternalSystemResponse(200, "OK");

    when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccountEntity));
    when(externalSystemRestClient.getExternalSystemResponse()).thenReturn(response);

    bankAccountService.debitMoney(1L, request);

    assertEquals(new BigDecimal("50.0000"), balance.getAmount());
    verify(balanceRepository).save(balance);
  }

  @Test
  void debitMoneyShouldThrowNotFoundWhenAccountDoesNotExist() {
    MoneyRequest request = new MoneyRequest();
    when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> bankAccountService.debitMoney(1L, request));
  }

  @Test
  void debitMoneyShouldThrowNotFoundWhenCurrencyDoesNotExist() {
    MoneyRequest request = new MoneyRequest();
    request.setCurrency(Currency.USD);

    when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccountEntity));

    assertThrows(NotFoundException.class, () -> bankAccountService.debitMoney(1L, request));
  }

  @Test
  void debitMoneyShouldThrowInsufficientFunds() {
    MoneyRequest request = new MoneyRequest();
    request.setAmount(new BigDecimal("150"));
    request.setCurrency(Currency.EUR);

    BalanceEntity balance = new BalanceEntity();
    balance.setCurrency(Currency.EUR);
    balance.setAmount(new BigDecimal("100"));
    bankAccountEntity.getBalances().add(balance);

    when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccountEntity));

    assertThrows(
        InsufficientFundsException.class, () -> bankAccountService.debitMoney(1L, request));
  }

  @Test
  void debitMoneyShouldThrowExternalSystemUnavailable() {
    MoneyRequest request = new MoneyRequest();
    request.setAmount(new BigDecimal("50"));
    request.setCurrency(Currency.EUR);

    BalanceEntity balance = new BalanceEntity();
    balance.setCurrency(Currency.EUR);
    balance.setAmount(new BigDecimal("100"));
    bankAccountEntity.getBalances().add(balance);

    ExternalSystemResponse response = new ExternalSystemResponse(500, "Service unavailable");

    when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccountEntity));
    when(externalSystemRestClient.getExternalSystemResponse()).thenReturn(response);

    assertThrows(
        ExternalSystemUnavailableException.class, () -> bankAccountService.debitMoney(1L, request));
  }

  @Test
  void shouldGetBalance() {
    BalanceEntity balance = new BalanceEntity();
    balance.setCurrency(Currency.EUR);
    balance.setAmount(new BigDecimal("100"));
    bankAccountEntity.setBalances(List.of(balance));

    BalanceResponse response = new BalanceResponse();
    response.setCurrency(Currency.EUR);
    response.setBalance(new BigDecimal("100"));

    when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccountEntity));
    when(balanceMapper.toResponse(balance)).thenReturn(response);

    List<BalanceResponse> results = bankAccountService.getBalance(1L);

    assertEquals(1, results.size());
    assertEquals(Currency.EUR, results.getFirst().getCurrency());
    assertEquals(new BigDecimal("100"), results.getFirst().getBalance());
  }

  @Test
  void getBalanceShouldThrowNotFoundWhenAccountDoesNotExist() {
    when(bankAccountRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> bankAccountService.getBalance(1L));
  }

  @Test
  void shouldExchangeCurrency() {
    BalanceEntity fromBalance = new BalanceEntity();
    fromBalance.setCurrency(Currency.USD);
    fromBalance.setAmount(new BigDecimal("100"));

    BalanceEntity toBalance = new BalanceEntity();
    toBalance.setCurrency(Currency.EUR);
    toBalance.setAmount(new BigDecimal("50"));

    bankAccountEntity.getBalances().addAll(List.of(fromBalance, toBalance));

    when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccountEntity));

    bankAccountService.exchangeCurrency(1L, Currency.USD, Currency.EUR, new BigDecimal("10"));

    // USD rate to EUR is 0.85. 10 USD * 0.85 = 8.5 EUR
    assertEquals(new BigDecimal("90.0000"), fromBalance.getAmount());
    assertEquals(new BigDecimal("58.5000"), toBalance.getAmount());
    verify(balanceRepository).saveAll(any());
  }

  @Test
  void exchangeCurrencyShouldCreateTargetBalanceIfMissing() {
    BalanceEntity fromBalance = new BalanceEntity();
    fromBalance.setCurrency(Currency.EUR);
    fromBalance.setAmount(new BigDecimal("100"));
    bankAccountEntity.getBalances().add(fromBalance);

    when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccountEntity));

    bankAccountService.exchangeCurrency(1L, Currency.EUR, Currency.USD, new BigDecimal("8.5"));

    // EUR to USD rate: toEur is 1. fromEur for USD is amount / 0.85
    // 8.5 EUR -> 8.5 / 0.85 = 10 USD
    assertEquals(new BigDecimal("91.5000"), fromBalance.getAmount());

    ArgumentCaptor<List<BalanceEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(balanceRepository).saveAll(captor.capture());

    List<BalanceEntity> savedBalances = captor.getValue();
    assertEquals(2, savedBalances.size());
    BalanceEntity savedToBalance =
        savedBalances.stream()
            .filter(b -> b.getCurrency().equals(Currency.USD))
            .findFirst()
            .orElseThrow();
    assertEquals(new BigDecimal("10.0000"), savedToBalance.getAmount());
  }

  @Test
  void exchangeCurrencyShouldDoNothingWhenCurrenciesAreSame() {
    bankAccountService.exchangeCurrency(1L, Currency.EUR, Currency.EUR, new BigDecimal("100"));
    verify(bankAccountRepository, never()).findById(any());
  }

  @Test
  void exchangeCurrencyShouldThrowInsufficientFunds() {
    BalanceEntity fromBalance = new BalanceEntity();
    fromBalance.setCurrency(Currency.EUR);
    fromBalance.setAmount(new BigDecimal("50"));
    bankAccountEntity.getBalances().add(fromBalance);

    when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccountEntity));

    assertThrows(
        InsufficientFundsException.class,
        () ->
            bankAccountService.exchangeCurrency(
                1L, Currency.EUR, Currency.USD, new BigDecimal("100")));
  }
}
