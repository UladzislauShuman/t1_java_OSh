package ru.t1.java.demo.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomingTransactionDto {
    private UUID accountId;
    private UUID transactionId;
    private UUID clientId;
    private BigDecimal amount;
}
