package ru.t1.java.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.java.demo.dto.TransactionResultDto;
import ru.t1.java.demo.dto.TransactionVerificationDto;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionAcceptanceServiceTest {
    @Mock
    private KafkaTemplate<String, TransactionResultDto> kafkaTemplate;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @InjectMocks
    private TransactionAcceptanceService transactionAcceptanceService;

    private final UUID clientId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transactionAcceptanceService, "maxTransaction", 5);
        ReflectionTestUtils.setField(transactionAcceptanceService, "timeWindowSeconds", 60L);

        Map<String, List<LocalDateTime>> recentTransactionMap = (Map<String, List<LocalDateTime>>)
                ReflectionTestUtils.getField(transactionAcceptanceService, "recentTransactionsMap");
        if (recentTransactionMap != null) {
            recentTransactionMap.clear();
        }
    }

    @Test
    void rejectedWhenInsufficientFunds() throws JsonProcessingException {
        TransactionVerificationDto request = TransactionVerificationDto.builder()
                .clientId(clientId)
                .accountId(accountId)
                .transactionId(transactionId)
                .transactionAmount(new BigDecimal("-100.00"))
                .accountBalance(new BigDecimal("50.00"))
                .build();
        String message = objectMapper.writeValueAsString(request);

        // перехват объекта, что отправлен в кафку
        ArgumentCaptor<TransactionResultDto> resultCaptor = ArgumentCaptor.forClass(TransactionResultDto.class);

        transactionAcceptanceService.handlerTransactionVerificationRequest(message);

        verify(kafkaTemplate, times(1)).send(eq(TransactionAcceptanceService.TOPIC_TRANSACTION_RESULT),
                resultCaptor.capture());

        TransactionResultDto resultDto = resultCaptor.getValue();

        assertEquals(transactionId, resultDto.getTransactionId());
        assertEquals(TransactionResultDto.Status.REJECTED, resultDto.getStatus());
        assertEquals(TransactionAcceptanceService.REASON_REJECTED_MESSAGE, resultDto.getReason());
    }

    @Test
    void acceptedWhenValidTransaction() throws JsonProcessingException {
        TransactionVerificationDto request = TransactionVerificationDto.builder()
                .clientId(clientId)
                .accountId(accountId)
                .transactionId(transactionId)
                .transactionAmount(new BigDecimal("200.00"))
                .accountBalance(new BigDecimal("500.00"))
                .build();

        String message = objectMapper.writeValueAsString(request);

        ArgumentCaptor<TransactionResultDto>resultCapture = ArgumentCaptor.forClass(TransactionResultDto.class);

        transactionAcceptanceService.handlerTransactionVerificationRequest(message);

        verify(kafkaTemplate, times(1)).send(
                eq(TransactionAcceptanceService.TOPIC_TRANSACTION_RESULT), resultCapture.capture());
        TransactionResultDto resultDto = resultCapture.getValue();

        assertEquals(transactionId, resultDto.getTransactionId());
        assertEquals(TransactionResultDto.Status.ACCEPTED, resultDto.getStatus());
        assertEquals(TransactionAcceptanceService.REASON_ACCEPTED_MESSAGE, resultDto.getReason());
    }

    @Test
    void blockedWhenLimitExceed() throws JsonProcessingException {
        String clientAccountId = clientId.toString() + ":" + accountId.toString();
        Map<String, List<LocalDateTime>> recentTransactionsMap = (Map<String, List<LocalDateTime>>)
                ReflectionTestUtils.getField(transactionAcceptanceService, "recentTransactionsMap");

        LocalDateTime now = LocalDateTime.now();
        recentTransactionsMap.put(clientAccountId, List.of(
                now.minusSeconds(10),
                now.minusSeconds(20),
                now.minusSeconds(30),
                now.minusSeconds(40),
                now.minusSeconds(50)
        ));

        TransactionVerificationDto request = TransactionVerificationDto.builder()
                .clientId(clientId)
                .accountId(accountId)
                .transactionId(transactionId)
                .transactionAmount(new BigDecimal("10.00"))
                .accountBalance(new BigDecimal("1000.00"))
                .timestamp(now)
                .build();
        String message = objectMapper.writeValueAsString(request);

        ArgumentCaptor<TransactionResultDto> resultCapture = ArgumentCaptor.forClass(TransactionResultDto.class);

        transactionAcceptanceService.handlerTransactionVerificationRequest(message);

        verify(kafkaTemplate, times(1)).send(eq(TransactionAcceptanceService.TOPIC_TRANSACTION_RESULT),
                resultCapture.capture());
        TransactionResultDto transactionResultDto = resultCapture.getValue();

        assertEquals(transactionId, transactionResultDto.getTransactionId());
        assertEquals(TransactionResultDto.Status.BLOCKED, transactionResultDto.getStatus());
        assertEquals(String.format(TransactionAcceptanceService.REASON_BLOCKED_MESSAGE, 6, 60L), transactionResultDto.getReason());
    }
}