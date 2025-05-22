package ru.t1.java.demo.util;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Transaction;

@Component
public class TransactionMapper {

    public static Transaction toEntity(ru.t1.java.demo.dto.TransactionDto dto) {
        return Transaction.builder()
                .accountId(dto.getAccountId())
                .amount(dto.getAmount())
                .time(dto.getTime())
                .build();
    }

    public static TransactionDto toDto(Transaction entity) {
        return ru.t1.java.demo.dto.TransactionDto.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .amount(entity.getAmount())
                .time(entity.getTime())
                .build();
    }
}
