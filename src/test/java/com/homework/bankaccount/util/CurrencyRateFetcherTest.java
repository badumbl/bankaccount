package com.homework.bankaccount.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.httpclient.FrankfurterRestClient;
import com.homework.bankaccount.httpclient.response.FrankfurterResponse;
import com.homework.bankaccount.service.CurrencyRateService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrencyRateFetcherTest {

  @Mock private FrankfurterRestClient frankfurterRestClient;
  @Mock private CurrencyRateService currencyRateService;
  @InjectMocks private CurrencyRateFetcher currencyRateFetcher;

  @Test
  void shouldFetchAndUpdateRatesForAllBaseCurrencies() {
    FrankfurterResponse response =
        new FrankfurterResponse(
            "EUR",
            Map.of(Currency.USD, new BigDecimal("1.08"), Currency.GBP, new BigDecimal("0.85")));
    when(frankfurterRestClient.getLatestRates(anyString(), anyString())).thenReturn(response);

    currencyRateFetcher.fetchAndUpdateRates();

    // Called once per Currency enum value
    verify(frankfurterRestClient, times(Currency.values().length))
        .getLatestRates(anyString(), anyString());
    // 2 rates × 4 base currencies = 8 updates
    verify(currencyRateService, times(Currency.values().length * 2))
        .updateRate(any(), any(), any());
  }

  @Test
  void shouldPassCorrectBaseAndSymbolsToClient() {
    FrankfurterResponse response = new FrankfurterResponse("EUR", Map.of());
    when(frankfurterRestClient.getLatestRates(anyString(), anyString())).thenReturn(response);

    currencyRateFetcher.fetchAndUpdateRates();

    // EUR base should never include EUR in symbols
    verify(frankfurterRestClient, never()).getLatestRates(eq("EUR"), contains("EUR"));
    verify(frankfurterRestClient, never()).getLatestRates(eq("USD"), contains("USD"));
  }

  @Test
  void shouldContinueFetchingWhenOneBaseCurrencyFails() {
    // EUR call throws, all others succeed
    when(frankfurterRestClient.getLatestRates(eq("EUR"), anyString()))
        .thenThrow(new RuntimeException("API timeout"));
    FrankfurterResponse response =
        new FrankfurterResponse("USD", Map.of(Currency.EUR, new BigDecimal("0.92")));
    when(frankfurterRestClient.getLatestRates(argThat(b -> !b.equals("EUR")), anyString()))
        .thenReturn(response);

    // Should not throw — failures are caught per-currency
    assertDoesNotThrow(() -> currencyRateFetcher.fetchAndUpdateRates());

    // The remaining 3 currencies (USD, GBP, SEK) still get their rates saved
    verify(currencyRateService, times(3)).updateRate(any(), any(), any());
  }
}
