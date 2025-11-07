package ru.t1.java.demo.unlock_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;


import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "app.services.unlock-service.url=http://localhost:${wiremock.server.port}"
})
class UnlockServiceTest {

    @Autowired
    private UnlockService unlockService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void callUnlockServiceAndParseSuccessResponse() throws Exception {
        UUID clientId = UUID.randomUUID();
        String url = "/api/unlock/client/" + clientId;

        UnlockDecisionDto mockResponse = UnlockDecisionDto.builder()
                .entityId(clientId)
                .entityType(UnlockDecisionDto.EntityType.CLIENT)
                .shouldUnlock(true)
                .reason("Approved")
                .build();

        stubFor(post(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        Optional<UnlockDecisionDto> result = unlockService.getClientUnlockDecision(clientId);

        assertTrue(result.isPresent());
        assertEquals(clientId, result.get().getEntityId());
        assertTrue(result.get().isShouldUnlock());

        verify(1, postRequestedFor(urlEqualTo(url)));
    }

    @Test
    void shouldReturnEmptyOptionalWhenServiceReturnsError() {
        UUID clientId = UUID.randomUUID();
        String url = "/api/unlock/client/" + clientId;

        stubFor(post(urlEqualTo(url))
                .willReturn(aResponse().withStatus(500)));

        Optional<UnlockDecisionDto> result = unlockService.getClientUnlockDecision(clientId);

        assertFalse(result.isPresent());

        verify(1, postRequestedFor(urlEqualTo(url)));
    }
}