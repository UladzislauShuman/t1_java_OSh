package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import r1.t1.monitoring.starter.annotation.Metric;
import ru.t1.java.demo.kafka.IncomingTransactionDto;
import ru.t1.java.demo.kafka.KafkaProducerService;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/test/kafka")
@RequiredArgsConstructor
public class TestKafkaController {
    public static final String TOPIC_TRANSACTIONS = "t1_demo_transactions";

    private final KafkaProducerService kafkaProducerService;
    private final AccountRepository accountRepository;

    @Metric
    @GetMapping("/send-transaction")
    public ResponseEntity<?> sendTestTransaction(
            @RequestParam(required = false) String accountUuid,
            @RequestParam(defaultValue = "50.25") String amountStr,
            @RequestParam(defaultValue = "false") boolean randomAccount
    ) {
        Optional<Account> optionalAccount = resolveTargetAccount(accountUuid, randomAccount);
        if (optionalAccount.isEmpty()) {
            return ResponseEntity.badRequest().body("No valid account found.");
        }

        Optional<BigDecimal> optionalAmount = parseAmount(amountStr);
        if (optionalAmount.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid amount format: " + amountStr);
        }

        return processTransaction(optionalAccount.get(), optionalAmount.get());
    }

    private Optional<Account> resolveTargetAccount(String accountUuid, boolean randomAccount) {
        if (randomAccount) return getRandomAccount();
        if (isValidAccountId(accountUuid)) return getAccountByUuid(accountUuid);
        return getFirstAvailableAccount();
    }

    private Optional<Account> getRandomAccount() {
        List<Account> accounts = (List<Account>) accountRepository.findAll();
        if (accounts.isEmpty()) return Optional.empty();
        Account random = accounts.get(new Random().nextInt(accounts.size()));
        log.error("Using random account: {}", random);
        return Optional.of(random);
    }

    private Optional<Account> getAccountByUuid(String uuid) {
        try {
            UUID accountId = UUID.fromString(uuid);
            return accountRepository.findByAccountId(accountId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<Account> getFirstAvailableAccount() {
        List<Account> accounts = (List<Account>) accountRepository.findAll();
        if (accounts.isEmpty()) return Optional.empty();
        Account first = accounts.get(0);
        log.error("Using first available account: {}", first);
        return Optional.of(first);
    }


    private ResponseEntity<?> processTransaction(Account account, BigDecimal amount) {
        IncomingTransactionDto dto = getIncomingTransactionDto(account.getAccountId(), account.getClientId(), amount);
        try {
            if (sendToKafka(dto)) {
                return sendSuccess(dto);
            } else {
                return sendError("Failed to send transaction to Kafka: " + dto);
            }
        } catch (Exception e) {
            return sendError("Kafka exception: " + e.getMessage());
        }
    }


    private Optional<BigDecimal> parseAmount(String amountStr) {
        try {
            return Optional.of(new BigDecimal(amountStr));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }


    private IncomingTransactionDto getIncomingTransactionDto(UUID accountId, UUID clientId, BigDecimal amount) {
        return IncomingTransactionDto.builder()
                .transactionId(UUID.randomUUID())
                .accountId(accountId)
                .clientId(clientId)
                .amount(amount)
                .build();
    }

    private boolean sendToKafka(IncomingTransactionDto incomingTransactionDto) {
        return kafkaProducerService.sendMessage(
                TOPIC_TRANSACTIONS,
                incomingTransactionDto,
                "TEST_TRANSACTION_REQUEST"
        );
    }

    private ResponseEntity sendSuccess(IncomingTransactionDto incomingTransactionDto) {
        String successMessage = "Test request with transaction sent to Kafka: " + incomingTransactionDto.toString();
        log.error(successMessage);
        return ResponseEntity.ok(successMessage);
    }

    private ResponseEntity sendError(String errorMessage) {
        log.error(errorMessage);
        return ResponseEntity.status(500).body(errorMessage);
    }

    private boolean isValidAccountId(String accountId) {
        return accountId != null && !accountId.isEmpty();
    }

}
