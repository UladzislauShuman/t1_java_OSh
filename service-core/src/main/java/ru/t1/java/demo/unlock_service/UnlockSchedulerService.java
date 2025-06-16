package ru.t1.java.demo.unlock_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduler.unlock.enabled", havingValue = "true", matchIfMissing = true)
public class UnlockSchedulerService {
    public static final String MESSAGE_ARRESTED_ACCOUNTS_EMPTY = "no accounts arrested";
    public static final String MESSAGE_BLOCKED_CLIENTS_EMPTY = "no blocked clients";
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final UnlockService unlockService;

    @Value("${app.scheduler.unlock.client-batch-size}")
    private int clientBatchSize;
    @Value("${app.scheduler.unlock.account-batch-size}")
    private int accountBatchSize;

    @Scheduled(fixedDelayString = "${app.scheduler.unlock.fixed-delay-ms}")
    @Transactional
    public void scheduledClientUnlocking() {
        log.error("Starting scheduled task: Client Unlocking");
        Pageable pageable = PageRequest.of(0, clientBatchSize);
        List<Client> blockedClients = clientRepository.findByStatus(Client.Status.BLOCKED, pageable).getContent();

        if (isBlockedClientsEmpty(blockedClients)) {
            return;
        }

        for (Client client : blockedClients) {
            getDecision(client);
        }
        log.error("finished scheduled task: client unlocking");
    }

    @Scheduled(fixedDelayString = "${app.scheduler.unlock.fixed-delay-ms}")
    @Transactional
    public void scheduledAccountUnlocking() {
        log.error("Starting scheduled task: Account unarresting");
        Pageable pageable = PageRequest.of(0, accountBatchSize);
        List<Account> arrestedAccounts = accountRepository.findByStatus(Account.Status.ARRESTED, pageable).getContent();

        if (isArrestedAccountsEmpty(arrestedAccounts)) {
            return;
        }

        for (Account account : arrestedAccounts) {
            getDecision(account);
        }
        log.error("finished scheduled task:  account unarresting");
    }

    private boolean isBlockedClientsEmpty(List<Client> blockedClients) {
        if (blockedClients.isEmpty()) {
            log.error(MESSAGE_BLOCKED_CLIENTS_EMPTY);
            return true;
        } else {
            log.error("found {} blocked clients to check for unlocking", blockedClients.size());
            return false;
        }
    }

    private boolean isArrestedAccountsEmpty(List<Account> arrestedAccounts) {
        if (arrestedAccounts.isEmpty()) {
            log.error(MESSAGE_ARRESTED_ACCOUNTS_EMPTY);
            return true;
        } else {
            log.error("found {} arrested accounts to check for unarresting", arrestedAccounts.size());
            return false;
        }
    }

    private void getDecision(Client client) {
        unlockService.getClientUnlockDecision(client.getClientId()).ifPresent(decision -> {
            log.error("decision for client {}: {}, reason: {}", client.getClientId(), decision.isShouldUnlock(),
                    decision.getReason());
            if (decision.isShouldUnlock()) {
                setClientsStatusActive(client);
            }
        });
    }

    private void getDecision(Account account) {
        unlockService.getAccountUnlockDecision(account.getAccountId()).ifPresent(decision -> {
            log.error("Decision for account {}: {}, reason: {}",
                    account.getAccountId(), decision.isShouldUnlock(), decision.getReason());
            if (decision.isShouldUnlock()) {
                setAccountStatusOpen(account);
            }
        });
    }

    private void setClientsStatusActive(Client client) {
        if (client != null) {
            client.setStatus(Client.Status.ACTIVE);
            clientRepository.save(client);
            log.error("client {} has been unlocked", client.getClientId());
        }
    }

    private void setAccountStatusOpen(Account account) {
        if (account != null) {
            account.setStatus(Account.Status.OPEN);
            accountRepository.save(account);
            log.error("Account {} has been unarrested", account.getAccountId());
        }
    }
}
