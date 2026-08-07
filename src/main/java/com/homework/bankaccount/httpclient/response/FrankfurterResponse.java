package com.homework.bankaccount.httpclient.response;

import com.homework.bankaccount.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class FrankfurterResponse {
     String base;
     Map<Currency, BigDecimal> rates;
}
