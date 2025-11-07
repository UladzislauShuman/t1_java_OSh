package ru.t1.java.demo.unlock_service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnlockDecisionDto {
    public enum EntityType {
        CLIENT,
        ACCOUNT
    }

    private UUID entityId;

    @JsonProperty("entity_type")
    private EntityType entityType;

    private boolean shouldUnlock;
    private String reason;
}

