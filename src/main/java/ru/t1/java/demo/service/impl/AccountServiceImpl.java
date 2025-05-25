package ru.t1.java.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;

import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.util.AccountMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements ImplementService<Account, AccountDto> {
    private final AccountRepository repository;
    private final ObjectMapper mapper;

    @PostConstruct
    void init() {
        List<Account> accounts = new ArrayList<>();
        try {
            accounts = parseJson();
        } catch (IOException e) {
            log.error("Ошибка во время обработки записей", e);
        }
        repository.saveAll(accounts);
    }

    @Override
//    @LogExecution
//    @Track
//    @HandlingResult
    public List<Account> parseJson() throws IOException {
        AccountDto[] clients = mapper.readValue(new File("src/main/resources/MOCK_DATA_ACCOUNTS.json"), AccountDto[].class);

        return Arrays.stream(clients)
                .map(AccountMapper::toEntity)
                .collect(Collectors.toList());
    }
}
