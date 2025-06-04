package ru.t1.java.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class Account extends AbstractPersistable<Long> implements HasId{

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "account_type")
    private AccountType accountType;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "status")
    private Status status;

    @Column(name = "account_id", unique = true, nullable = false)
    private UUID accountId;

    @Column(name = "frozen_amount")
    private BigDecimal frozenAmount;

    public enum AccountType {
        DEBIT,
        CREDIT
    }
    public enum Status {
        OPEN,
        ARRESTED,
        BLOCKED,
        CLOSED
    }
}
