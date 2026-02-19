package com.homework.bankaccount.entities;

import com.homework.bankaccount.enums.Currency;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "balance")
@Entity
public class BalanceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "amount", precision = 19, scale = 4)
  private BigDecimal amount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(name = "currency")
  private Currency currency;

  @ManyToOne
  @JoinColumn(name = "bank_account_id")
  private BankAccountEntity bankAccount;
}
