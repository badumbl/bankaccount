package com.homework.bankaccount.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "external.system")
public class ExternalSystemConfig {

  private String url;
}
