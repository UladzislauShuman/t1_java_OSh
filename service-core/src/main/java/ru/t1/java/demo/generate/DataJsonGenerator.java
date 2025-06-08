package ru.t1.java.demo.generate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class DataJsonGenerator {
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        List<ClientDto> clients = new ArrayList<>();
        List<AccountDto> accounts = new ArrayList<>();
        List<TransactionDto> transactions = new ArrayList<>();

        Random random = new Random();
        long minDayEpochSecond = LocalDateTime.now()
                .minusYears(5)
                .toEpochSecond(ZoneOffset.UTC);

        long maxDayEpochSecond = LocalDateTime.now()
                .toEpochSecond(ZoneOffset.UTC);

        for (int i = 1; i <= 1000; i++) {
            UUID clientId = UUID.randomUUID();

            Client.Status clientStatus;

            if (random.nextInt(10) == 0) {
                clientStatus = Client.Status.BLOCKED;
            } else {
                clientStatus = Client.Status.ACTIVE;
            }

            ClientDto clientDto = ClientDto.builder()
                    .id((long) i)
                    .firstName("Name " + i)
                    .lastName("Surname " + i)
                    .middleName("Patronymic " + i)
                    .clientId(clientId)
                    .status(clientStatus)
                    .build();
            clients.add(clientDto);

            Account.Status accountStatus = clientStatus == Client.Status.BLOCKED ? Account.Status.BLOCKED : Account.Status.OPEN;

            AccountDto accountDto = AccountDto.builder()
                    .id((long) i)
                    .clientId(clientId)
                    .accountType(random.nextBoolean() ? Account.AccountType.DEBIT : Account.AccountType.CREDIT)
                    .balance(BigDecimal.valueOf(random.nextDouble() * 10000).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .accountId(UUID.randomUUID())
                    .status(accountStatus)
                    .frozenAmount(BigDecimal.ZERO)
                    .build();
            accounts.add(accountDto);

            int numberOfTransactions = random.nextInt(5) + 1;
            for (int j = 0; j < numberOfTransactions; j++) {
                long transactionEpochSecond = minDayEpochSecond + random.nextLong(maxDayEpochSecond - minDayEpochSecond);
                LocalDateTime transactionTime = LocalDateTime.ofEpochSecond(transactionEpochSecond, 0, ZoneOffset.UTC);

                Transaction.Status randomStatus;
                int statusRoll = random.nextInt(10);
                if (statusRoll < 7) {
                    randomStatus = Transaction.Status.ACCEPTED;
                } else if (statusRoll < 9) {
                    randomStatus = Transaction.Status.REQUESTED;
                } else {
                    randomStatus = Transaction.Status.REJECTED;
                }

                TransactionDto transactionDto = TransactionDto.builder()
                        .id((long) (transactions.size() + 1))
                        .accountId(accountDto.getId())
                        .amount(BigDecimal.valueOf(random.nextDouble() * (random.nextBoolean() ? 100 : -50))
                                .setScale(2, BigDecimal.ROUND_HALF_UP))
                        .timestamp(transactionTime)
                        .transactionId(UUID.randomUUID())
                        .status(randomStatus)
                        .build();
                transactions.add(transactionDto);
            }
        }

        String basePath = "service-core/src/main/resources/";
        File fileClients = new File(basePath + "MOCK_DATA_CLIENTS.json");
        File fileAccounts = new File(basePath + "MOCK_DATA_ACCOUNTS.json");
        File fileTransactions = new File(basePath + "MOCK_DATA_TRANSACTIONS.json");

        objectMapper.writeValue(fileClients, clients);
        objectMapper.writeValue(fileAccounts, accounts);
        objectMapper.writeValue(fileTransactions, transactions);

        log.error("generated {} Clients, {} Accounts, and {} Transactions", clients.size(), accounts.size(), transactions.size());
    }
}