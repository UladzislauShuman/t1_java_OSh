package ru.t1.java.unlock.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.unlock.dto.UnlockDecisionDto;
import ru.t1.java.unlock.service.DecisionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/unlock")
@RequiredArgsConstructor
public class UnlockController {
    public static final String DECISION_CLIENT_UNLOCKED = "Client is unlocked";
    public static final String DECISION_CLIENT_UNLOCK_DENIED = "Client unlock denied";
    public static final String DECISION_ACCOUNT_ARRESTED = "Account is unlocked";
    public static final String DECISION_ACCOUNT_UNARRESTED = "account unlock denied";

    private final DecisionService decisionService;

    @PostMapping("/client/{clientId}")
    public ResponseEntity<UnlockDecisionDto> getClientUnlockDecision(
            @PathVariable UUID clientId) {
        boolean decision = decisionService.shouldUnlock();
        String reason = getReason(decision, UnlockDecisionDto.EntityType.CLIENT);

        UnlockDecisionDto response = getUnlockDecisionDto(
                clientId, UnlockDecisionDto.EntityType.CLIENT,
                decision, reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/account/{accountId}")
    public ResponseEntity<UnlockDecisionDto> getAccountUnlockDecision(
            @PathVariable UUID accountId) {
        boolean decision = decisionService.shouldUnlock();
        String reason = getReason(decision, UnlockDecisionDto.EntityType.ACCOUNT);

        UnlockDecisionDto response = getUnlockDecisionDto(
                accountId, UnlockDecisionDto.EntityType.ACCOUNT,
                decision, reason);

        return ResponseEntity.ok(response);
    }

    private String getReason(boolean decision, UnlockDecisionDto.EntityType entityType) {
        switch (entityType) {
            case CLIENT -> {
                return decision ? DECISION_CLIENT_UNLOCKED : DECISION_CLIENT_UNLOCK_DENIED;
            }
            case ACCOUNT -> {
                return decision ? DECISION_ACCOUNT_ARRESTED : DECISION_ACCOUNT_UNARRESTED;
            }
            default -> {
                return "bad type";
            }
        }
    }

    private UnlockDecisionDto getUnlockDecisionDto(UUID uuid, UnlockDecisionDto.EntityType entityType,
                                                   boolean decision, String reason) {
        return UnlockDecisionDto.builder()
                .entityId(uuid)
                .entityType(UnlockDecisionDto.EntityType.CLIENT)
                .shouldUnlock(decision)
                .reason(reason)
                .build();
    }
}
