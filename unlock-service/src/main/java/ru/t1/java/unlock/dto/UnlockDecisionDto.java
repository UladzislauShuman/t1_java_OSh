package ru.t1.java.unlock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnlockDecisionDto {
    public enum EntityType {
        CLIENT,
        ACCOUNT
    }

    private UUID entityId;
    private EntityType entityType;
    private boolean shouldUnlock;
    private String reason;
}
