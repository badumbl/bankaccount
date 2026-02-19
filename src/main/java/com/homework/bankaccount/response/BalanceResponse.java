package com.homework.bankaccount.response;

import com.homework.bankaccount.enums.Currency;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

  private BigDecimal balance;
  private Currency currency;
}
