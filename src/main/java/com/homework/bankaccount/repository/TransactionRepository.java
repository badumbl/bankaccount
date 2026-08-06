package com.homework.bankaccount.repository;

import com.homework.bankaccount.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findByBankAccountIdOrderByCreatedAtDesc(Long bankAccountId);
}
