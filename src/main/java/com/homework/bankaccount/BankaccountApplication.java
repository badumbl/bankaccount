package com.homework.bankaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BankaccountApplication {

  public static void main(String[] args) {
    SpringApplication.run(BankaccountApplication.class, args);
  }
}
