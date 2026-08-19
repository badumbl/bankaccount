package com.homework.bankaccount.event;

import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.enums.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEvent(
        Long accountId,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        BigDecimal targetAmount,
        Currency targetCurrency,
        Instant occurredAt
) {}
