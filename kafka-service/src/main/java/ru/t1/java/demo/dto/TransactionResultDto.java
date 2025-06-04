package ru.t1.java.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResultDto {
    public enum Status {
        ACCEPTED,
        REJECTED,
        BLOCKED
    }

    private UUID transactionId;
    private UUID accountId;
    private Status status;
    private BigDecimal amountInvolved;
    private String reason;
}
