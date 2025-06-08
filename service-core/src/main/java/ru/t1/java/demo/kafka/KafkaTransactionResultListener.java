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

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTransactionResultListener {

    private final ObjectMapper objectMapper;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public static final String TOPIC_TRANSACTION_RESULT = "t1_demo_transaction_result";

    @KafkaListener(topics = TOPIC_TRANSACTION_RESULT, groupId = "transaction-result-group")
    @Transactional
    @Metric
    public void handleTransactionResult(String message) {
        try {
            TransactionResultDto resultDto = readTransactionResultDto(message);
            Transaction transaction = getTransactionFromTransactionResultDto(resultDto);

            if (isTransactionNull(transaction, resultDto)) {
                return;
            }

            Account account = getAccountFromTransactionResultDto(resultDto);
            
            if (isAccountNull(account, transaction, resultDto)) {
                return;
            }

            doDependingOnTransactionResultStatus(transaction, resultDto, account);

        } catch (Exception e) {
            log.error("Error processing transaction result message: {}", message, e);
        }
    }

    private TransactionResultDto readTransactionResultDto(String message) throws JsonProcessingException {
        TransactionResultDto resultDto =  objectMapper.readValue(message, TransactionResultDto.class);
        log.info("Received transaction result: {}", resultDto);
        return resultDto;
    }

    private Transaction getTransactionFromTransactionResultDto(TransactionResultDto transactionResultDto) {
         return transactionRepository.findByTransactionId(transactionResultDto.getTransactionId()).orElse(null);
    }

    private boolean isTransactionNull(Transaction transaction, TransactionResultDto transactionResultDto) {
        if (transaction == null) {
            log.error("Transaction with UUID {} not found. Ignoring result.", transactionResultDto.getTransactionId());
            return true;
        }
        return false;
    }

    private Account getAccountFromTransactionResultDto(TransactionResultDto transactionResultDto) {
        return accountRepository.findByAccountId(transactionResultDto.getAccountId())
                .orElse(null);
    }

    private boolean isAccountNull(Account account, Transaction transaction, TransactionResultDto transactionResultDto) {
        if (account == null) {
            log.error("Account with UUID {} for transaction {} not found. Inconsistent state.",
                    transactionResultDto.getAccountId(), transactionResultDto.getTransactionId());
            transaction.setStatus(Transaction.Status.REJECTED);
            transactionRepository.save(transaction);
            return  true;
        }
        return false;
    }

    private void doDependingOnTransactionResultStatus(
            Transaction transaction, TransactionResultDto transactionResultDto, Account account) {
        switch (transactionResultDto.getStatus()) {
            case ACCEPTED:
                doIfStatusIsAccepted(transaction);
                break;
            case REJECTED:
                doIfStatusIsRejected(transaction, transactionResultDto, account);
                break;
            case BLOCKED:
                doIfStatusIsBlocked(transaction, transactionResultDto, account);
                break;
        }
    }

    private void doIfStatusIsAccepted(Transaction transaction) {
        transaction.setStatus(Transaction.Status.ACCEPTED);
        transactionRepository.save(transaction);
        log.info("Transaction {} status updated to ACCEPTED.", transaction.getTransactionId());
    }

    private void doIfStatusIsRejected(
            Transaction transaction, TransactionResultDto transactionResultDto, Account account) {
        transaction.setStatus(Transaction.Status.REJECTED);
        saveTransactionWhenRejected(transaction, transactionResultDto);
        setLastAccountBalanceAndSave(account, transactionResultDto);
    }

    private void  saveTransactionWhenRejected(Transaction transaction, TransactionResultDto transactionResultDto) {
        transactionRepository.save(transaction);
        log.info("Transaction {} status updated to REJECTED. Reason: {}",
                transaction.getTransactionId(), transactionResultDto.getReason());
    }

    private void setLastAccountBalanceAndSave(Account account, TransactionResultDto transactionResultDto) {
        BigDecimal amountToRevert = transactionResultDto.getAmountInvolved();
        account.setBalance(account.getBalance().subtract(amountToRevert));
        accountRepository.save(account);
        log.info("Account {} balance reverted by {} due to REJECTED transaction. New balance: {}",
                account.getAccountId(), amountToRevert, account.getBalance());
    }

    private void doIfStatusIsBlocked(
            Transaction transaction, TransactionResultDto transactionResultDto, Account account) {
        saveTransactionWhenBlocked(transaction, transactionResultDto);
        account.setStatus(Account.Status.BLOCKED);
        setAccountBalanceWhenBlocked(account, transactionResultDto);

    }

    private void saveTransactionWhenBlocked(Transaction transaction, TransactionResultDto transactionResultDto) {
        transaction.setStatus(Transaction.Status.BLOCKED);
        transactionRepository.save(transaction);
        log.info("Transaction {} status updated to BLOCKED. Reason: {}",
                transaction.getTransactionId(), transactionResultDto.getReason());
    }

    public void setAccountBalanceWhenBlocked(Account account, TransactionResultDto transactionResultDto) {
        BigDecimal blockedAmount = transactionResultDto.getAmountInvolved();
        account.setBalance(account.getBalance().subtract(blockedAmount));
        account.setFrozenAmount(account.getFrozenAmount().add(blockedAmount.abs()));

        accountRepository.save(account);
        log.info("Account {} status set to BLOCKED. Balance adjusted by {}, frozenAmount increased by {}. New balance: {}, New frozen: {}",
                account.getAccountId(), blockedAmount.negate(), blockedAmount.abs(), account.getBalance(), account.getFrozenAmount());

    }
}