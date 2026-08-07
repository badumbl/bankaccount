package com.homework.bankaccount.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.exception.BadRequestException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class CurrencyRateServiceTest {

  @Mock private StringRedisTemplate redisTemplate;
  @Mock private HashOperations<String, Object, Object> hashOperations;
  @InjectMocks private CurrencyRateService currencyRateService;

  @Test
  void shouldUpdateRate() {
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);

    currencyRateService.updateRate(Currency.EUR, Currency.USD, new BigDecimal("1.08"));

    verify(hashOperations).put("currency_rates", "EUR:USD", "1.08");
  }

  @Test
  void shouldGetRate() {
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    when(hashOperations.get("currency_rates", "EUR:USD")).thenReturn("1.08");

    BigDecimal rate = currencyRateService.getRate(Currency.EUR, Currency.USD);

    assertEquals(new BigDecimal("1.08"), rate);
  }

  @Test
  void shouldReturnOneWhenCurrenciesAreTheSame() {
    BigDecimal rate = currencyRateService.getRate(Currency.EUR, Currency.EUR);

    assertEquals(BigDecimal.ONE, rate);
    verifyNoInteractions(redisTemplate);
  }

  @Test
  void shouldThrowWhenRateNotFound() {
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    when(hashOperations.get("currency_rates", "EUR:USD")).thenReturn(null);

    assertThrows(
        BadRequestException.class, () -> currencyRateService.getRate(Currency.EUR, Currency.USD));
  }
}
