package com.homework.bankaccount.mapper;

import com.homework.bankaccount.entities.TransactionEntity;
import com.homework.bankaccount.response.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(TransactionEntity entity) {
        return new TransactionResponse(
                entity.getId(),
                entity.getType(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getTargetAmount(),
                entity.getTargetCurrency(),
                entity.getCreatedAt()
        );
    }
}
