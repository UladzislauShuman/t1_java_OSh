package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.t1.java.demo.model.Account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for {@link Account}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDto implements Serializable {
    private Long id;
    @JsonProperty("client_id")
    private UUID clientId;
    @JsonProperty("account_type")
    private Account.AccountType accountType;
    @JsonProperty("balance")
    private BigDecimal balance;
    @JsonProperty("status")
    private Account.Status status;
    @JsonProperty("account_id")
    private UUID accountId;
    @JsonProperty("frozen_amount")
    private BigDecimal frozenAmount;
}