package com.homework.bankaccount.config;

import com.homework.bankaccount.httpclient.FrankfurterRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class FrankfurterRestClientConfig {

  @Bean
  public FrankfurterRestClient frankfurterRestClient() {
    RestClient restClient =
        RestClient.builder()
            .baseUrl("https://api.frankfurter.app")
            .requestInterceptor(
                (request, body, execution) -> {
                  var response = execution.execute(request, body);
                  log.info("Frankfurter API response status: {}", response.getStatusCode());
                  return response;
                })
            .build();
    RestClientAdapter adapter = RestClientAdapter.create(restClient);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
    return factory.createClient(FrankfurterRestClient.class);
  }
}
