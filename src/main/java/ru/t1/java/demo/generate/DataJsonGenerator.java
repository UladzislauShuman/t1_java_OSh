package ru.t1.java.demo.generate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Account;

public class DataJsonGenerator {
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());

        List<AccountDto> accounts = new ArrayList<>();
        List<TransactionDto> transactions = new ArrayList<>();

        Random random = new Random();
        long minDay = LocalDateTime.now()
                .minusYears(5)
                .toEpochSecond(ZoneOffset.UTC);

        long maxDay = LocalDateTime.now()
                .toEpochSecond(ZoneOffset.UTC);



        for (int i = 1; i <= 1000; i++) {

            AccountDto accountDto = AccountDto.builder()
                    .id((long) i)
                    .clientId((long) i)
                    .accountType(random.nextBoolean() ? Account.AccountType.DEBIT : Account.AccountType.CREDIT)
                    .balance(BigDecimal.valueOf(random.nextInt(10000)))
                    .build();
            accounts.add(accountDto);

            TransactionDto transactionDto = TransactionDto.builder()
                    .id((long) i)
                    .accountId((long) i)
                    .amount(BigDecimal.valueOf(random.nextInt(100)))
                    .time(
                            LocalDateTime.ofEpochSecond(
                                    minDay + random.nextLong(maxDay - minDay),
                                    0,
                                    ZoneOffset.UTC
                            )
                    )
                    .build();
            transactions.add(transactionDto);
        }

        File fileAccounts = new File("src/main/resources/MOCK_DATA_ACCOUNTS.json");
        File fileTransactions = new File("src/main/resources/MOCK_DATA_TRANSACTIONS.json");

        objectMapper.writeValue(fileAccounts, accounts);
        objectMapper.writeValue(fileTransactions, transactions);
    }
}
