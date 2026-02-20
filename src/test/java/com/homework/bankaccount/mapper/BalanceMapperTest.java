package com.homework.bankaccount.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.homework.bankaccount.entities.BalanceEntity;
import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.response.BalanceResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BalanceMapperTest {
  private final BalanceMapper balanceMapper = new BalanceMapper();

  @Test
  void shouldMapToResponse() {
    BalanceEntity balanceEntity = new BalanceEntity();
    balanceEntity.setAmount(new BigDecimal("100"));
    balanceEntity.setId(1L);
    balanceEntity.setCurrency(Currency.EUR);

    BalanceResponse balanceResponse = balanceMapper.toResponse(balanceEntity);

    assertEquals(new BigDecimal("100"), balanceResponse.getBalance());
    assertEquals(Currency.EUR, balanceResponse.getCurrency());
  }
}
