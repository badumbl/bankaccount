package com.homework.bankaccount.config;

import com.homework.bankaccount.httpclient.ExternalSystemRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ExternalSystemRestClientConfig {

  private final ExternalSystemConfig externalSystemConfig;

  @Bean
  public ExternalSystemRestClient externalSystemRestClient() {
    RestClient restClient =
        RestClient.builder()
            .requestInterceptor(createInterceptorForLogging())
            .requestFactory(new SimpleClientHttpRequestFactory())
            .baseUrl(externalSystemConfig.getUrl())
            .build();
    RestClientAdapter adapter = RestClientAdapter.create(restClient);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
    return factory.createClient(ExternalSystemRestClient.class);
  }

  private ClientHttpRequestInterceptor createInterceptorForLogging() {
    return (request, body, execution) -> {
      ClientHttpResponse response = execution.execute(request, body);
      log.info("External System response status: {}", response.getStatusCode());
      return response;
    };
  }
}
