package com.homework.bankaccount.httpclient;

import com.homework.bankaccount.httpclient.response.ExternalSystemResponse;
import org.springframework.web.service.annotation.GetExchange;

public interface ExternalSystemRestClient {
  @GetExchange(accept = "application/json", url = "/random/200")
  ExternalSystemResponse getExternalSystemResponse();
}
