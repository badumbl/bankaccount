package com.homework.bankaccount.httpclient;

import com.homework.bankaccount.httpclient.response.FrankfurterResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface FrankfurterRestClient {

    @GetExchange("/latest")
    FrankfurterResponse getLatestRates(
            @RequestParam String base,
            @RequestParam String symbols
    );
}
