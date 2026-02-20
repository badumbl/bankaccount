package com.homework.bankaccount.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(@NotBlank String name) {}
