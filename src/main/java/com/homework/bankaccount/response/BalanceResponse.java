package com.homework.bankaccount.response;

import com.homework.bankaccount.enums.Currency;
import java.math.BigDecimal;

public record BalanceResponse(BigDecimal balance, Currency currency) {}
