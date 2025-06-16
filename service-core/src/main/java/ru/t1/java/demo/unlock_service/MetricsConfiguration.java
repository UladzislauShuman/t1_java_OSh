package ru.t1.java.demo.unlock_service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.repository.ClientRepository;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MetricsConfiguration {

    public static final String METRIC_ACCOUNTS_ARRESTED_DESCRIPTION = "The current number of arrested accounts";
    public static final String METRIC_CLIENTS_BLOCKED_DESCRIPTION = "The current number of blocked clients";
    public static final String METRIC_ACCOUNTS_ARRESTED_NAME = "application.accounts.arrested.count";
    public static final String METRIC_CLIENTS_BLOCKED_NAME = "application.clients.blocked.count";

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    @Bean
    public MeterBinder blockedClientsCountMeterBinder() {
        log.error("blockedClientsCountMeterBinder");
        return (registry) -> Gauge.builder(METRIC_CLIENTS_BLOCKED_NAME, this,
                        self -> self.fetchBlockedClientsCount())
                .description(METRIC_CLIENTS_BLOCKED_DESCRIPTION)
                .register(registry);
    }

    @Bean
    public MeterBinder arrestedAccountsCountMeterBinder() {
        log.error("arrestedAccountsCountMeterBinder");
        return (registry) -> Gauge.builder(METRIC_ACCOUNTS_ARRESTED_NAME, this,
                        self -> self.fetchArrestedAccountsCount())
                .description(METRIC_ACCOUNTS_ARRESTED_DESCRIPTION)
                .register(registry);
    }

    public double fetchBlockedClientsCount() {
        try {
            return clientRepository.countByStatus(Client.Status.BLOCKED);
        } catch (Exception e) {
            return -1.0;
        }
    }

    public double fetchArrestedAccountsCount() {
        try {
            return accountRepository.countByStatus(Account.Status.ARRESTED);
        } catch (Exception e) {
            return -1.0;
        }
    }
}