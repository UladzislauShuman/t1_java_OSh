package ru.t1.java.demo.unlock_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UnlockService {
    public static final String API_UNLOCK = "/api/unlock/";
    public static final String CLIENT = "client/";
    public static final String ACCOUNT = "account/";
    private final RestTemplate restTemplate;

    @Value("${app.services.unlock-service.url}")
    private String unlockServiceUrl;

    public Optional<UnlockDecisionDto> getClientUnlockDecision(UUID clientId) {
        String url = unlockServiceUrl + API_UNLOCK + CLIENT + clientId;
        return getUnlockDecision(url);
    }

    public Optional<UnlockDecisionDto> getAccountUnlockDecision(UUID account) {
        String url = unlockServiceUrl + API_UNLOCK + ACCOUNT + account;
        return getUnlockDecision(url);
    }


    private Optional<UnlockDecisionDto> getUnlockDecision(String url) {
        try {
            UnlockDecisionDto response = getUnlockDecisionDto(url);
            return Optional.ofNullable(response);
        } catch (RestClientException e) {
            log.error("Failed to take unlock from {}, Reason {}", url, e.getMessage());
            return Optional.empty();
        }
    }

    private UnlockDecisionDto getUnlockDecisionDto(String url) {
        log.error("Requesting unlock decision from {}", url);
        return restTemplate.postForObject(url, null, UnlockDecisionDto.class);
    }
}
