package ru.t1.java.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transaction")
public class Transaction extends AbstractPersistable<Long> implements HasId {

    public enum Status {
        REQUESTED,
        ACCEPTED,
        REJECTED,
        BLOCKED,
        CANCELLED
    }

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private UUID transactionId;
}
