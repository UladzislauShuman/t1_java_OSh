package ru.t1.java.demo.util;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Transaction;

@Component
public class TransactionMapper {

    public static Transaction toEntity(TransactionDto dto) {
        return Transaction.builder()
                .accountId(dto.getAccountId())
                .amount(dto.getAmount())
                .status(dto.getStatus())
                .timestamp(dto.getTimestamp())
                .transactionId(dto.getTransactionId())
                .build();
    }

    public static TransactionDto toDto(Transaction entity) {
        return TransactionDto.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .timestamp(entity.getTimestamp())
                .transactionId(entity.getTransactionId())
                .build();
    }
}
