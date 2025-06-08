package ru.t1.java.demo.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlackListService {
    public static final String EXCEPTION_CLIENT_ID_IS_NULL = "clientId is null";
    private final RestTemplate restTemplate;

    @Value("${app.services.fraud-detection.url}")
    private String fraudServiceUrl;

    public boolean isClientBlackListed(UUID clientId) {
        try {
            BlackListResponseDto responseDto = getBlackListResponseDto(clientId, getUrl(clientId));

            if (responseDto != null) {
                log.error("client with id {} blacklist status: ", clientId, responseDto.isBlackListed());
                return responseDto.isBlackListed();
            }
        } catch (RestClientException e) {
            log.error("Exception calling fraud service for client {}, {}", clientId, e.getMessage());
            return false;
        }
        return false;
    }

    private String getUrl(UUID clientId) {
        if (clientId == null)
            throw new NullPointerException(EXCEPTION_CLIENT_ID_IS_NULL);
        String url = fraudServiceUrl + "/api/clients/" + clientId + "/check-blacklist";
        return url;
    }

    private BlackListResponseDto getBlackListResponseDto(UUID clientId, String url) {
        log.error("Checking of client with id: {} for blacklist status at {}", clientId, url);
        BlackListResponseDto responseDto = restTemplate.getForObject(url, BlackListResponseDto.class);
        return responseDto;
    }
}
