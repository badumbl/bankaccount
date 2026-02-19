package com.homework.bankaccount.exception;

public class ExternalSystemUnavailableException extends RuntimeException {
  public ExternalSystemUnavailableException(String message) {
    super(message);
  }
}
