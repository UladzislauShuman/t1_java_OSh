package ru.t1.java.demo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:9096", "port=9096" },
        topics = { KafkaTransactionResultListener.TOPIC_TRANSACTION_RESULT }
)
class KafkaTransactionResultListenerTest {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private Account account;
    private Transaction transaction;
    private final BigDecimal initialBalance = new BigDecimal("900.00");
    private final BigDecimal transactionAmount = new BigDecimal("-100.00");

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        account = accountRepository.save(Account.builder()
                .accountId(UUID.randomUUID())
                .clientId(UUID.randomUUID())
                .status(Account.Status.OPEN)
                .balance(initialBalance) // уже уменьшили
                .frozenAmount(BigDecimal.ZERO)
                .build());

        transaction = transactionRepository.save(Transaction.builder()
                .transactionId(UUID.randomUUID())
                .accountId(account.getId())
                .amount(transactionAmount)
                .status(Transaction.Status.REQUESTED)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Test
    void whenResultIsAccepted_thenTransactionStatusIsUpdated() throws JsonProcessingException {
        TransactionResultDto transactionResultDto = TransactionResultDto.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(account.getAccountId())
                .status(TransactionResultDto.Status.ACCEPTED)
                .amountInvolved(transactionAmount)
                .reason("ok")
                .build();

        kafkaTemplate.send(KafkaTransactionResultListener.TOPIC_TRANSACTION_RESULT,
                objectMapper.writeValueAsString(transactionResultDto));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
            Account finalAccount = accountRepository.findById(account.getId()).orElseThrow();

            assertEquals(Transaction.Status.ACCEPTED, updatedTransaction.getStatus());
            assertEquals(0, initialBalance.compareTo(finalAccount.getBalance()));
        });
    }

    @Test
    void whenResultIsRejected_thenStatusIsUpdatedAndBalanceReverted() throws JsonProcessingException {
        TransactionResultDto transactionResultDto = TransactionResultDto.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(account.getAccountId())
                .status(TransactionResultDto.Status.REJECTED)
                .amountInvolved(transactionAmount)
                .reason("rejected")
                .build();

        kafkaTemplate.send(KafkaTransactionResultListener.TOPIC_TRANSACTION_RESULT,
                objectMapper.writeValueAsString(transactionResultDto));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
            Account finalAccount = accountRepository.findById(account.getId()).orElseThrow();

            assertEquals(Transaction.Status.REJECTED, updatedTransaction.getStatus());
            BigDecimal expectedBalance = initialBalance.subtract(transactionAmount);
            assertEquals(0, expectedBalance.compareTo(finalAccount.getBalance()));
        });
    }

    @Test
    void whenResultIsBlocked_thenStatusIsUpdatedAndAccountBlocked() throws JsonProcessingException {
        TransactionResultDto transactionResultDto = TransactionResultDto.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(account.getAccountId())
                .status(TransactionResultDto.Status.BLOCKED)
                .amountInvolved(transactionAmount)
                .reason("blocked")
                .build();

        kafkaTemplate.send(KafkaTransactionResultListener.TOPIC_TRANSACTION_RESULT,
                objectMapper.writeValueAsString(transactionResultDto));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
            Account finalAccount = accountRepository.findById(account.getId()).orElseThrow();

            assertEquals(Transaction.Status.BLOCKED, updatedTransaction.getStatus());
            assertEquals(Account.Status.BLOCKED, finalAccount.getStatus());

            BigDecimal expectedBalance = initialBalance.subtract(transactionAmount);
            assertEquals(0, expectedBalance.compareTo(finalAccount.getBalance()));

            assertEquals(0, transactionAmount.abs().compareTo(finalAccount.getFrozenAmount()));
        });
    }
}