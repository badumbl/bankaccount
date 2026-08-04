package com.homework.bankaccount;

import com.homework.bankaccount.service.CurrencyRateService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BankaccountApplicationTests {

  @MockitoBean
  CurrencyRateService currencyRateService;

  @Test
  void contextLoads() {}
}
