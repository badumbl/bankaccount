package com.homework.bankaccount.entities;

import com.homework.bankaccount.enums.Currency;
import com.homework.bankaccount.enums.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "transaction")
@Entity
public class TransactionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bank_account_id", nullable = false)
  private BankAccountEntity bankAccount;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private TransactionType type;

  @Column(name = "amount", precision = 19, scale = 4)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "currency")
  private Currency currency;

  // Only populated for EXCHANGE transactions
  @Column(name = "target_amount", precision = 19, scale = 4)
  private BigDecimal targetAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_currency")
  private Currency targetCurrency;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
