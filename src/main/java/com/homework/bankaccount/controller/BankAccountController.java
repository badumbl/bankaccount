package com.homework.bankaccount.controller;

import com.homework.bankaccount.entities.BankAccountEntity;
import com.homework.bankaccount.request.CreateAccountRequest;
import com.homework.bankaccount.request.CurrencyExchangeRequest;
import com.homework.bankaccount.request.MoneyRequest;
import com.homework.bankaccount.response.BalanceResponse;
import com.homework.bankaccount.service.BankAccountService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bankaccount")
public class BankAccountController {

  private final BankAccountService bankAccountService;

  @PostMapping()
  public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest request) {
    BankAccountEntity account = bankAccountService.createAccount(request.name());
    return ResponseEntity.ok(account.getId());
  }

  @PostMapping(value = "/{id}/deposit")
  public ResponseEntity<?> addMoney(
      @PathVariable Long id, @Valid @RequestBody MoneyRequest request) {
    bankAccountService.addMoney(id, request);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/{id}/debit")
  public ResponseEntity<?> debitMoney(
      @PathVariable Long id, @Valid @RequestBody MoneyRequest request) {
    bankAccountService.debitMoney(id, request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<List<BalanceResponse>> getAccountBalance(@PathVariable Long id) {
    List<BalanceResponse> balances = bankAccountService.getBalance(id);
    return ResponseEntity.ok(balances);
  }

  @PostMapping("/{id}/currency")
  public ResponseEntity<?> getCurrencyExchange(
      @PathVariable Long id, @Valid @RequestBody CurrencyExchangeRequest request) {
    bankAccountService.exchangeCurrency(
        id, request.fromCurrency(), request.toCurrency(), request.amount());
    return ResponseEntity.ok().build();
  }
}
