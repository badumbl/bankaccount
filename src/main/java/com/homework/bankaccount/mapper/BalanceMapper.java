package com.homework.bankaccount.mapper;

import com.homework.bankaccount.entities.BalanceEntity;
import com.homework.bankaccount.response.BalanceResponse;
import org.springframework.stereotype.Component;

@Component
public class BalanceMapper {

  public BalanceResponse toResponse(BalanceEntity balanceEntity) {
    return new BalanceResponse(balanceEntity.getAmount(), balanceEntity.getCurrency());
  }
}
