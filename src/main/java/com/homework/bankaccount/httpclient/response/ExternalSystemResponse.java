package com.homework.bankaccount.httpclient.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExternalSystemResponse {
  private int code;
  private String description;
}
