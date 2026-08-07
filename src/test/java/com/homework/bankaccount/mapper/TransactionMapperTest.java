package com.homework.bankaccount.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.homework.bankaccount.entities.TransactionEntity;
import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.enums.TransactionType;
import com.homework.bankaccount.response.TransactionResponse;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TransactionMapperTest {
  private final TransactionMapper transactionMapper = new TransactionMapper();

  @Test
  void shouldMapDepositTransactionToResponse() {
    TransactionEntity transactionEntity = new TransactionEntity();
    transactionEntity.setId(1L);
    transactionEntity.setType(TransactionType.DEPOSIT);
    transactionEntity.setAmount(new BigDecimal("100"));
    transactionEntity.setCurrency(Currency.EUR);
    transactionEntity.setTargetAmount(null);
    transactionEntity.setTargetCurrency(null);
    Instant now = Instant.now();
    transactionEntity.setCreatedAt(now);

    TransactionResponse response = transactionMapper.toResponse(transactionEntity);

    assertEquals(1L, response.id());
    assertEquals(TransactionType.DEPOSIT, response.type());
    assertEquals(new BigDecimal("100"), response.amount());
    assertEquals(Currency.EUR, response.currency());
    assertNull(response.targetAmount());
    assertNull(response.targetCurrency());
    assertEquals(now, response.createdAt());
  }

  @Test
  void shouldMapExchangeTransactionToResponse() {
    TransactionEntity entity = new TransactionEntity();
    entity.setId(2L);
    entity.setType(TransactionType.EXCHANGE);
    entity.setAmount(new BigDecimal("50.00"));
    entity.setCurrency(Currency.EUR);
    entity.setTargetAmount(new BigDecimal("57.50"));
    entity.setTargetCurrency(Currency.USD);
    entity.setCreatedAt(Instant.now());

    TransactionResponse response = transactionMapper.toResponse(entity);

    assertEquals(TransactionType.EXCHANGE, response.type());
    assertEquals(new BigDecimal("50.00"), response.amount());
    assertEquals(Currency.EUR, response.currency());
    assertEquals(new BigDecimal("57.50"), response.targetAmount());
    assertEquals(Currency.USD, response.targetCurrency());
  }
}
