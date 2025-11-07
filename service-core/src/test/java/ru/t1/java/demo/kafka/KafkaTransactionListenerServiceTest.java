package ru.t1.java.demo.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.security.BlackListResponseDto;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = { "listeners=PLAINTEXT://localhost:9095", "port=9095" },
        topics = {KafkaTransactionListenerService.TOPIC_TRANSACTIONS,
                KafkaTransactionListenerService.TOPIC_TRANSACTION_ACCEPT}
)
@TestPropertySource(properties = {
    "app.services.fraud-detection.url=http://localhost:${wiremock.server.port}",
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class KafkaTransactionListenerServiceTest {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private Consumer<String, String> verificationConsumer;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        clientRepository.deleteAll();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group-listener",
                "true", (EmbeddedKafkaBroker) embeddedKafka);
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        verificationConsumer = consumerFactory.createConsumer();
        verificationConsumer.subscribe(Collections.singletonList(KafkaTransactionListenerService.TOPIC_TRANSACTION_ACCEPT));
    }

    @AfterEach
    void tearDown() {
        if (verificationConsumer != null) {
            verificationConsumer.close();
        }
    }

    @Test
    void whenClientIsBlacklisted_thenTransactionRejectedAndEntitiesBlocked() throws Exception {
        Client client = clientRepository.save(Client.builder().clientId(UUID.randomUUID()).status(Client.Status.ACTIVE).build());
        Account account = accountRepository.save(Account.builder()
                .accountId(UUID.randomUUID())
                .clientId(client.getClientId())
                        .status(Account.Status.OPEN)
                        .balance(new BigDecimal("1000"))
                        .frozenAmount(BigDecimal.ZERO)
                .build());

        stubFor(get(urlEqualTo("/api/clients/" + client.getClientId() + "/check-blacklist"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(new BlackListResponseDto(client.getClientId(), true
                        )))));
        IncomingTransactionDto incomingTransactionDto =
                IncomingTransactionDto.builder().transactionId(UUID.randomUUID()).clientId(client.getClientId())
                        .accountId(account.getAccountId()).amount(new BigDecimal("-100")).build();

        kafkaTemplate.send(KafkaTransactionListenerService.TOPIC_TRANSACTIONS, objectMapper.writeValueAsString(incomingTransactionDto));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();
            Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();

            assertEquals(Client.Status.BLOCKED, updatedClient.getStatus());
            assertEquals(Account.Status.BLOCKED, updatedAccount.getStatus());
            assertEquals(1, transactionRepository.count());

            Transaction createdTransaction = transactionRepository.findAll().iterator().next();
            assertEquals(Transaction.Status.REJECTED, createdTransaction.getStatus());
        });

        verify(1, getRequestedFor(urlPathEqualTo("/api/clients/" + client.getClientId() + "/check-blacklist")));

    }

    @Test
    void whenClientIsNotBlacklisted_thenTransactionSentToVerification() throws Exception {
        Client client = clientRepository.save(Client.builder().clientId(UUID.randomUUID()).status(Client.Status.ACTIVE).build());
        Account account = accountRepository.save(Account.builder()
                .accountId(UUID.randomUUID())
                .clientId(client.getClientId())
                .status(Account.Status.OPEN).balance(new BigDecimal("1000"))
                .frozenAmount(BigDecimal.ZERO)
                .build());

        stubFor(get(urlPathEqualTo("/api/clients/" + client.getClientId() + "/check-blacklist"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(new BlackListResponseDto(client.getClientId(), false)))));

        BigDecimal amount = new BigDecimal("-100.00");
        IncomingTransactionDto transactionDto = IncomingTransactionDto.builder()
                .transactionId(UUID.randomUUID())
                .clientId(client.getClientId())
                .accountId(account.getAccountId())
                .amount(amount)
                .build();

        kafkaTemplate.send(KafkaTransactionListenerService.TOPIC_TRANSACTIONS,
                objectMapper.writeValueAsString(transactionDto));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
            Transaction createdTransaction = transactionRepository.findAll().iterator().next();

            assertEquals(0, new BigDecimal("900.00").compareTo(updatedAccount.getBalance()));
            assertEquals(Transaction.Status.REQUESTED, createdTransaction.getStatus());
        });

        ConsumerRecord<String, String> verificationRecord = KafkaTestUtils.getSingleRecord(verificationConsumer,
                KafkaTransactionListenerService.TOPIC_TRANSACTION_ACCEPT, Duration.ofSeconds(10));
        assertNotNull(verificationRecord);
        assertTrue(verificationRecord.value().contains(account.getAccountId().toString()));

        verify(1, getRequestedFor(urlPathEqualTo("/api/clients/" + client.getClientId() + "/check-blacklist")));
    }
}