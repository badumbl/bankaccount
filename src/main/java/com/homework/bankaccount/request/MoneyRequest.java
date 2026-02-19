package com.homework.bankaccount.request;

import com.homework.bankaccount.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class MoneyRequest {
  @NotNull
  @DecimalMin("0.01")
  private BigDecimal amount;

  @NotNull private Currency currency;
}
