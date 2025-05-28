package ru.t1.java.demo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.my.Metric;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaTransactionListenerService {

    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaProducerService kafkaProducerService;

    public static final String TOPIC_TRANSACTIONS = "t1_demo_transactions";
    public static final String TOPIC_TRANSACTION_ACCEPT = "t1_demo_transaction_accept";

    @KafkaListener(topics = TOPIC_TRANSACTIONS, groupId = "transaction-request-group")
    @Transactional
    @Metric
    public void handleTransaction(String message) {
        try {
            IncomingTransactionDto incomingTransactionDto = readIncomingTransactionDto(message);
            Account account = findAccountFromIncomingTransaction(incomingTransactionDto);
            if (!isAccountGood(account, incomingTransactionDto)) {
                return;
            }
            if (isAccountStatusOPEN(account)) {
                Transaction transaction = getTransaction(account, incomingTransactionDto);
                saveTransactionToRepository(transaction);
                setNewBalanceToAccountAndSave(account, incomingTransactionDto);
                sendMessageToKafka(account, transaction);
            } else {
                log.error("the status of Account {} is not OPEN (it is {}), transaction {} will not be processed",
                        account.getAccountId(), account.getStatus(), incomingTransactionDto.getTransactionId());
            }
        } catch (Exception e) {
            log.error("exception in processing transaction request message: {}", message,e);
        }
    }

    private IncomingTransactionDto readIncomingTransactionDto(String message) throws JsonProcessingException {
        IncomingTransactionDto incomingTransactionDto = objectMapper.readValue(message, IncomingTransactionDto.class);
        log.error("received transaction: {}", incomingTransactionDto);
        return incomingTransactionDto;
    }

    private Account findAccountFromIncomingTransaction(IncomingTransactionDto incomingTransactionDto) {
        Account account = accountRepository.findByAccountId(incomingTransactionDto.getAccountId()).orElse(null);
        return account;
    }

    private boolean isAccountGood(Account account, IncomingTransactionDto incomingTransactionDto) {
        if (account == null) {
            log.error("no account with id: {}, ignoring of transaction", incomingTransactionDto.getAccountId());
            return false;
        }
        if (!isAccountClientIdEqualsToIncomingTransaction(account, incomingTransactionDto)) {
            log.error("ClientId mismatch of account {}: Account {}, Incoming: {}",
                    incomingTransactionDto.getAccountId(), account.getClientId(), incomingTransactionDto.getClientId());
            return false;
        }
        return true;
    }

    private boolean isAccountClientIdEqualsToIncomingTransaction(Account account, IncomingTransactionDto incomingTransactionDto) {
        return account.getClientId().equals(incomingTransactionDto.getClientId());
    }

    private boolean isAccountStatusOPEN(Account account) {
        return account.getStatus() == Account.Status.OPEN;
    }

    private Transaction getTransaction(Account account, IncomingTransactionDto incomingTransactionDto) {
        return Transaction.builder()
                .accountId(account.getId())
                .transactionId(UUID.randomUUID())
                .amount(incomingTransactionDto.getAmount())
                .timestamp(LocalDateTime.now())
                .status(Transaction.Status.REQUESTED)
                .build();
    }

    private void saveTransactionToRepository(Transaction transaction) {
        transactionRepository.save(transaction);
        log.error("transaction {} saved with satus REQUESTED", transaction.getTransactionId());
    }

    private void setNewBalanceToAccountAndSave(Account account, IncomingTransactionDto incomingTransactionDto) {
        BigDecimal newBalance = account.getBalance().add(incomingTransactionDto.getAmount());
        account.setBalance(newBalance);
        saveAccount(account, newBalance);
    }

    public void saveAccount(Account account, BigDecimal newBalance) {
        accountRepository.save(account);
        log.error("account {} saved with new balance: {}", account.getAccountId(),newBalance);
    }
    private TransactionVerificationDto getTransactionVerificationDto(Account account, Transaction transaction) {
        return TransactionVerificationDto.builder()
                .clientId(account.getClientId())
                .accountId(account.getAccountId())
                .transactionId(transaction.getTransactionId())
                .timestamp(transaction.getTimestamp())
                .transactionAmount(transaction.getAmount())
                .accountBalance(account.getBalance())
                .build();
    }

    private void sendMessageToKafka(Account account, Transaction transaction) {
        TransactionVerificationDto transactionVerificationDto =
                getTransactionVerificationDto(account, transaction);
        kafkaProducerService.sendMessage(
                TOPIC_TRANSACTION_ACCEPT,
                transactionVerificationDto,
                "TRANSACTION_VERIFICATION_REQUEST"
        );
        log.error("sent to {} for verification {}", TOPIC_TRANSACTION_ACCEPT, transactionVerificationDto);
    }
}
