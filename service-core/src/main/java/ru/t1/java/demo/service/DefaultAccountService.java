package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import r1.t1.monitoring.starter.annotation.LogDataSourceError;
import r1.t1.monitoring.starter.annotation.Metric;
import ru.t1.java.demo.aop.my.Cached;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.exception.AccountException;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.util.AccountMapper;

import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultAccountService extends AbstractCrudService<Account, AccountDto> implements AccountService {
    public static final String ACCOUNT_ID_IS_NULL = "accountId is null";
    public static final String ACCOUNT_NOT_FOUND = "account not found";
    private final AccountRepository repository;

    @Override
    protected CrudRepository<Account, Long> getRepository() {
        return repository;
    }

    @Override
    protected Function<Account, AccountDto> toDto() {
        return AccountMapper::toDto;
    }

    @Override
    protected Function<AccountDto, Account> toEntity() {
        return AccountMapper::toEntity;
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
    public Page<AccountDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(AccountMapper::toDto);
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
    public Optional<AccountDto> findById(Long id) {
        return super.findById(id);
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
    public List<AccountDto> findAllByIds(List<Long> ids) {
        return super.findAllByIds(ids);
    }

    @Override
    @LogDataSourceError
    @Metric
    public AccountDto save(AccountDto dto) {
        return super.save(dto);
    }

    @Override
    @LogDataSourceError
    @Metric
    public Iterable<AccountDto> saveAll(Collection<AccountDto> dtos) {
        return super.saveAll(dtos);
    }

    @Override
    @LogDataSourceError
    @Metric
    public void delete(AccountDto dto) {
        super.delete(dto);
    }

    @Override
    @LogDataSourceError
    @Metric
    public void deleteAllByIds(List<Long> ids) {
        super.deleteAllByIds(ids);
    }

    @Override
    @LogDataSourceError
    @Metric
    public AccountDto findByAccountId(UUID accountId) {
        if (accountId == null)
            throw new IllegalArgumentException(ACCOUNT_ID_IS_NULL);
        Optional<Account> account = repository.findByAccountId(accountId);
        if (account.isPresent()) {
            return AccountMapper.toDto(account.get());
        } else {
            throw new AccountException(ACCOUNT_NOT_FOUND);
        }
    }
}