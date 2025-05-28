package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.util.TransactionMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements ImplementService<Transaction, AccountDto> {
    private final TransactionRepository repository;
    private final ObjectMapper mapper;

    @PostConstruct
    void init() {
        List<Transaction> transactions = new ArrayList<>();
        try {
            transactions = parseJson();
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
        }
        repository.saveAll(transactions);
    }

    @Override
    public List<Transaction> parseJson() throws IOException {

        TransactionDto[] clients = mapper.readValue(new File("src/main/resources/MOCK_DATA_TRANSACTIONS.json"), TransactionDto[].class);

        return Arrays.stream(clients)
                .map(TransactionMapper::toEntity)
                .collect(Collectors.toList());
    }
}
