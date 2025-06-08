package ru.t1.java.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientBlackListController {

    private final BlackListService blackListService;

    @GetMapping("/{clientId}/check-blacklist")
    public ResponseEntity<BlackListResponseDto> checkClientIdInBlackList(
            @PathVariable UUID clientId) {
        boolean isBlackListed = blackListService.isClientBlackListed(clientId);
        BlackListResponseDto responseDto = BlackListResponseDto.builder()
                .clientId(clientId)
                .isBlackListed(isBlackListed)
                .build();

        return ResponseEntity.ok(responseDto);
    }
}
