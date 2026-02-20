package com.homework.bankaccount.request;

import com.homework.bankaccount.enums.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MoneyRequest(
    @NotNull @DecimalMin("0.01") BigDecimal amount, @NotNull Currency currency) {}
