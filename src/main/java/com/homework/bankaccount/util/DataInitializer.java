package com.homework.bankaccount.util;

import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.service.CurrencyRateService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CurrencyRateService currencyRateService;

    @Override
    public void run(ApplicationArguments args) {
        setRateIfAbsent(Currency.EUR, Currency.USD, new BigDecimal("1.15"));
        setRateIfAbsent(Currency.USD, Currency.EUR, new BigDecimal("0.87"));
        setRateIfAbsent(Currency.EUR, Currency.GBP, new BigDecimal("0.86"));
        setRateIfAbsent(Currency.GBP, Currency.EUR, new BigDecimal("1.17"));
        setRateIfAbsent(Currency.EUR, Currency.SEK, new BigDecimal("11.01"));
        setRateIfAbsent(Currency.SEK, Currency.EUR, new BigDecimal("0.091"));
        setRateIfAbsent(Currency.USD, Currency.SEK, new BigDecimal("9.57"));
        setRateIfAbsent(Currency.SEK, Currency.USD, new BigDecimal("0.10"));
        setRateIfAbsent(Currency.USD, Currency.GBP, new BigDecimal("0.74"));
        setRateIfAbsent(Currency.GBP, Currency.USD, new BigDecimal("1.34"));
        setRateIfAbsent(Currency.SEK, Currency.GBP, new BigDecimal("0.078"));
        setRateIfAbsent(Currency.GBP, Currency.SEK, new BigDecimal("12.86"));
    }

    private void setRateIfAbsent(Currency from, Currency to, BigDecimal rate) {
        try {
            currencyRateService.getRate(from, to);
        } catch (Exception e) {
            currencyRateService.updateRate(from, to, rate);
        }
    }
}
