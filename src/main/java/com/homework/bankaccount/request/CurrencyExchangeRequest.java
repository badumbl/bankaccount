package com.homework.bankaccount.request;

import com.homework.bankaccount.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CurrencyExchangeRequest {
  @NotNull private Currency fromCurrency;
  @NotNull private Currency toCurrency;

  @NotNull
  @DecimalMin("0.01")
  private BigDecimal amount;
}
