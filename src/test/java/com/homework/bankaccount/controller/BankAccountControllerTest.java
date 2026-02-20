package com.homework.bankaccount.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.homework.bankaccount.entities.BankAccountEntity;
import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.httpclient.ExternalSystemRestClient;
import com.homework.bankaccount.request.CreateAccountRequest;
import com.homework.bankaccount.request.CurrencyExchangeRequest;
import com.homework.bankaccount.request.MoneyRequest;
import com.homework.bankaccount.response.BalanceResponse;
import com.homework.bankaccount.service.BankAccountService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(BankAccountController.class)
class BankAccountControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BankAccountService bankAccountService;

  @MockitoBean private ExternalSystemRestClient externalSystemRestClient;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateAccount() throws Exception {
    BankAccountEntity bankAccountEntity = new BankAccountEntity();
    bankAccountEntity.setId(1L);
    bankAccountEntity.setName("test");

    CreateAccountRequest createAccountRequest = new CreateAccountRequest();
    createAccountRequest.setName("test");

    when(bankAccountService.createAccount("test")).thenReturn(bankAccountEntity);

    mockMvc
        .perform(
            post("/api/v1/bankaccount")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string("1"));

    verify(bankAccountService).createAccount("test");
  }

  @Test
  void shouldAddMoney() throws Exception {
    MoneyRequest moneyRequest = new MoneyRequest();
    moneyRequest.setAmount(new BigDecimal("100"));
    moneyRequest.setCurrency(Currency.EUR);

    mockMvc
        .perform(
            post("/api/v1/bankaccount/1/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moneyRequest)))
        .andExpect(status().isOk());

    verify(bankAccountService).addMoney(1L, moneyRequest);
  }

  @Test
  void shouldDebitMoney() throws Exception {
    MoneyRequest moneyRequest = new MoneyRequest();
    moneyRequest.setAmount(new BigDecimal("99"));
    moneyRequest.setCurrency(Currency.EUR);

    mockMvc
        .perform(
            post("/api/v1/bankaccount/1/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moneyRequest)))
        .andExpect(status().isOk());

    verify(bankAccountService).debitMoney(1L, moneyRequest);
  }

  @Test
  void shouldGetBalance() throws Exception {
    BalanceResponse balanceResponse = new BalanceResponse(new BigDecimal("100"), Currency.EUR);

    when(bankAccountService.getBalance(1L)).thenReturn(List.of(balanceResponse));

    mockMvc
        .perform(get("/api/v1/bankaccount/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].balance").value("100"))
        .andExpect(jsonPath("$[0].currency").value("EUR"));

    verify(bankAccountService).getBalance(1L);
  }

  @Test
  void shouldExchangeCurrency() throws Exception {
    CurrencyExchangeRequest currencyExchangeRequest = new CurrencyExchangeRequest();
    currencyExchangeRequest.setFromCurrency(Currency.EUR);
    currencyExchangeRequest.setToCurrency(Currency.USD);
    currencyExchangeRequest.setAmount(new BigDecimal("100"));

    mockMvc
        .perform(
            post("/api/v1/bankaccount/1/currency")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(currencyExchangeRequest)));

    verify(bankAccountService)
        .exchangeCurrency(1L, Currency.EUR, Currency.USD, new BigDecimal("100"));
  }
}
