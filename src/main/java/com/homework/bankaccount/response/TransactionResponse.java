package com.homework.bankaccount.response;

import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        BigDecimal targetAmount,
        Currency targetCurrency,
        Instant createdAt
) {}
