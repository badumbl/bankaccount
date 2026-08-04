package com.homework.bankaccount.controller;

import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.service.CurrencyRateService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rates")
public class CurrencyRateController {
  private final CurrencyRateService currencyRateService;

  @PutMapping("/{from}/{to}")
  public ResponseEntity<?> updateRate(
      @PathVariable Currency from, @PathVariable Currency to, @RequestBody BigDecimal rate) {
    currencyRateService.updateRate(from, to, rate);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{from}/{to}")
  public ResponseEntity<BigDecimal> getRate(@PathVariable Currency from, @PathVariable Currency to) {
    BigDecimal rate = currencyRateService.getRate(from, to);
    return ResponseEntity.ok(rate);
  }

}