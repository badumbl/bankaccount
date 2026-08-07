package com.homework.bankaccount.util;

import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.httpclient.FrankfurterRestClient;
import com.homework.bankaccount.httpclient.response.FrankfurterResponse;
import com.homework.bankaccount.service.CurrencyRateService;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyRateFetcher {

  private final FrankfurterRestClient frankfurterRestClient;
  private final CurrencyRateService currencyRateService;

  @Scheduled(fixedRateString = "${currency.rate.fetch.interval-ms:3600000}", initialDelay = 0)
  public void fetchAndUpdateRates() {
    log.info("Fetching currency rates from Frankfurter API...");

    int updated = 0;
    int failed = 0;

    for (Currency base : Currency.values()) {
      try {
        String allSymbols =
            Arrays.stream(Currency.values())
                .filter(c -> c != base)
                .map(Currency::name)
                .collect(Collectors.joining(","));
        FrankfurterResponse response =
            frankfurterRestClient.getLatestRates(base.name(), allSymbols);
        response
            .getRates()
            .forEach((currency, rate) -> currencyRateService.updateRate(base, currency, rate));
        updated += response.getRates().size();
      } catch (Exception e) {
        log.error("Failed to fetch currency rates for base currency: {}", base, e);
        failed++;
      }
    }

    log.info("Currency rates updated completed. Updated: {} Failed: {}", updated, failed);
  }
}
