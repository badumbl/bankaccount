package com.homework.bankaccount.service;

import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.exception.BadRequestException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyRateService {
  private final StringRedisTemplate redisTemplate;
  private static final String CURRENCY_RATE_KEY = "currency_rates";

  public void updateRate(Currency from, Currency to, BigDecimal rate) {
    String pairKey = from.name() + ":" + to.name();
    redisTemplate.opsForHash().put(CURRENCY_RATE_KEY, pairKey, rate.toString());
  }

  public BigDecimal getRate(Currency from, Currency to) {
    if (from == to) return BigDecimal.ONE;

    String pairKey = from.name() + ":" + to.name();
    Object rate = redisTemplate.opsForHash().get(CURRENCY_RATE_KEY, pairKey);
    if (rate == null) {
      throw new BadRequestException("Currency exchange rate not found for " + pairKey);
    }
    return new BigDecimal(rate.toString());
  }
}
