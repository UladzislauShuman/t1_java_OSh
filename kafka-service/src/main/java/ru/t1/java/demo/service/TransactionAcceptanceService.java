package ru.t1.java.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.TransactionResultDto;
import ru.t1.java.demo.dto.TransactionVerificationDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionAcceptanceService {
    public static final String REASON_BLOCKED_MESSAGE = "Too many transactions (%d) in the last %d seconds.";
    public static final String REASON_REJECTED_MESSAGE = "Insufficient funds.";
    public static final String REASON_ACCEPTED_MESSAGE = "transaction is accepted";
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, TransactionResultDto> kafkaTemplate;

    @Value("${app.fraud.max-transactions}")
    private int maxTransaction;

    @Value("${app.fraud.time-window-seconds}")
    private long timeWindowSeconds;

    public static final String TOPIC_TRANSACTION_ACCEPT = "t1_demo_transaction_accept";
    public static final String TOPIC_TRANSACTION_RESULT = "t1_demo_transaction_result";

    private final Map<String, List<LocalDateTime>> recentTransactionsMap = new ConcurrentHashMap<>();

    @KafkaListener(topics = TOPIC_TRANSACTION_ACCEPT, groupId = "${spring.kafka.consumer.group-id}")
    public void handlerTransactionVerificationRequest(String message) {
        try {
            TransactionVerificationDto request = readTransactionVerificationDto(message);
            TransactionResultDto.Status finalStatus = null;
            String reason = null;
            String clientAccountKey = getClientAccountKey(request);
            LocalDateTime now = request.getTimestamp();
            computeRecentTransactionsMap(recentTransactionsMap, now, clientAccountKey );
            StatusAndReason statusAndReason = getStatusAndReason(request, clientAccountKey);
            sendTransactionResultDto(request, statusAndReason.reason(), statusAndReason.status());
        } catch (Exception e) {
            log.error("exception in processing transaction request message: {}", message,e);
        }
    }

    private TransactionVerificationDto readTransactionVerificationDto(String message) throws JsonProcessingException {
        TransactionVerificationDto transactionVerificationDto =
                objectMapper.readValue(message, TransactionVerificationDto.class);
        log.error("received for verification {}", transactionVerificationDto);
        return transactionVerificationDto;
    }

    private void computeRecentTransactionsMap(
            Map<String, List<LocalDateTime>> recentTransactionsMap,
            LocalDateTime now,
            String clientAccountKey) {
        recentTransactionsMap.compute(clientAccountKey, (key, timestamps) -> {
            List<LocalDateTime> currentTimestamps = timestamps == null ? new ArrayList<>() : timestamps;
            List<LocalDateTime> freshTimestamps = currentTimestamps.stream()
                    .filter(ts -> ChronoUnit.SECONDS.between(ts, now) < timeWindowSeconds)
                    .collect(Collectors.toList());
            freshTimestamps.add(now);
            return freshTimestamps;
        });
    }

    private void sendTransactionResultDto(
            TransactionVerificationDto transactionVerificationDto,
            String reason,
            TransactionResultDto.Status status
            ) {
        TransactionResultDto resultDto =  TransactionResultDto.builder()
                .transactionId(transactionVerificationDto.getTransactionId())
                .accountId(transactionVerificationDto.getAccountId())
                .status(status)
                .amountInvolved(transactionVerificationDto.getTransactionAmount())
                .reason(reason)
                .build();

        kafkaTemplate.send(TOPIC_TRANSACTION_RESULT, resultDto);
        log.error("Sent transaction result to {}: {}", TOPIC_TRANSACTION_RESULT, resultDto);

    }

    private void loggingByFinalStatus(TransactionResultDto.Status status, TransactionVerificationDto dto, String reason) {
        switch (status) {
            case BLOCKED -> log.error("Transaction {} for account {} BLOCKED. {}",
                    dto.getTransactionId(), dto.getAccountId(), reason);
            case REJECTED -> log.error("Transaction {} for account {} REJECTED. Balance: {}, Amount: {}",
                    dto.getTransactionId(), dto.getAccountId(), dto.getAccountBalance(), dto.getTransactionAmount());
            case ACCEPTED -> log.error("Transaction {} for account {} ACCEPTED.",
                    dto.getTransactionId(), dto.getAccountId());
        }
    }

    private String getClientAccountKey(TransactionVerificationDto dto) {
        return dto.getClientId().toString() + ":" + dto.getAccountId().toString();
    }

    private boolean hasInsufficientFunds(TransactionVerificationDto dto) {
        return dto.getTransactionAmount().compareTo(BigDecimal.ZERO) < 0 &&
                dto.getAccountBalance().compareTo(dto.getTransactionAmount().abs()) < 0;
    }

    private StatusAndReason getStatusAndReason(
                                    TransactionVerificationDto request, String clientAccountKey) {
        List<LocalDateTime> clientTransactionInWindow = recentTransactionsMap.get(clientAccountKey);
        long countInWindow = clientTransactionInWindow.size();
        String reason;
        TransactionResultDto.Status status;
        if (countInWindow >= maxTransaction) {
            status = TransactionResultDto.Status.BLOCKED;
            reason = String.format(REASON_BLOCKED_MESSAGE,
                    countInWindow, timeWindowSeconds);
            loggingByFinalStatus(status, request, reason);
        } else if (hasInsufficientFunds(request)) {
            status = TransactionResultDto.Status.REJECTED;
            reason = REASON_REJECTED_MESSAGE;
            loggingByFinalStatus(status, request, reason);
        } else {
            status = TransactionResultDto.Status.ACCEPTED;
            reason = REASON_ACCEPTED_MESSAGE;
            loggingByFinalStatus(status, request, reason);
        }
        return new StatusAndReason(status, reason);
    }

    private record StatusAndReason(TransactionResultDto.Status status, String reason) {}
}
