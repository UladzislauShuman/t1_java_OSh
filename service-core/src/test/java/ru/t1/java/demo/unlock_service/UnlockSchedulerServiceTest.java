package ru.t1.java.demo.unlock_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.t1.java.demo.kafka.KafkaProducerService;
import ru.t1.java.demo.kafka.KafkaTransactionListenerService;
import ru.t1.java.demo.kafka.KafkaTransactionResultListener;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.ClientRepository;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.services.unlock-service.url=http://localhost:${wiremock.server.port}",
        "app.scheduler.unlock.enabled=true"
})
@DirtiesContext
class UnlockSchedulerServiceTest {

    @Autowired
    private UnlockSchedulerService unlockSchedulerService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaProducerService kafkaProducerService;
    @MockBean
    private KafkaTransactionListenerService kafkaTransactionListenerService;
    @MockBean
    private KafkaTransactionResultListener kafkaTransactionResultListener;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
    }

    @Test
    void unlockBlockedClientsAfterApproval() throws Exception {
        Client blockedClient = clientRepository.save(Client.builder()
                .clientId(UUID.randomUUID())
                .status(Client.Status.BLOCKED)
                .build());

        UnlockDecisionDto mockResponse = UnlockDecisionDto.builder()
                .shouldUnlock(true)
                .build();
        stubFor(post(urlPathEqualTo("/api/unlock/client/" + blockedClient.getClientId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));


        unlockSchedulerService.scheduledClientUnlocking();

        Client updatedClient = clientRepository.findById(blockedClient.getId()).orElseThrow();
        assertEquals(Client.Status.ACTIVE, updatedClient.getStatus());

        verify(1, postRequestedFor(urlPathEqualTo("/api/unlock/client/" + blockedClient.getClientId())));
    }
}