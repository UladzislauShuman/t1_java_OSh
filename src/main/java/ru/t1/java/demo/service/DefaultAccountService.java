package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.aop.my.LogDataSourceError;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.util.AccountMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultAccountService implements AccountService {
    private final AccountRepository repository;

    @Override
    @LogDataSourceError
    public Page<AccountDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(AccountMapper::toDto);
    }

    @Override
    @LogDataSourceError
    public Optional<AccountDto> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id == null");
        }
        Account account = repository.findById(id).orElse(null);
        return Optional.ofNullable(AccountMapper.toDto(account));
    }

    @Override
    @LogDataSourceError
    public List<AccountDto> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        Iterable<Account> accounts = repository.findAllById(ids);
        return StreamSupport.stream(accounts.spliterator(), false)
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @LogDataSourceError
    public AccountDto save(AccountDto accountDto) {
        if (accountDto == null) {
            throw new IllegalArgumentException("accountDto == null");
        }
        Account account = AccountMapper.toEntity(accountDto);
        repository.save(account);
        return accountDto;
    }

    @Override
    @LogDataSourceError
    public Iterable<AccountDto> saveAll(Collection<AccountDto> accountDtos) {
        if (accountDtos == null || accountDtos.isEmpty()) {
            return Collections.emptyList();
        }
        if (accountDtos.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("accountDtos has null object");
        }

        List<Account> accounts = (List<Account>) repository.saveAll(accountDtos.stream()
                .map(AccountMapper::toEntity)
                .collect(Collectors.toList()));
        return accounts.stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @LogDataSourceError
    public void delete(AccountDto accountDto) {
        if (accountDto == null) {
            throw new IllegalArgumentException("accountDto is null");
        }
        Optional<Account> account = repository.findById(AccountMapper.toEntity(accountDto).getId());
        account.ifPresent(repository::delete);
    }

    @Override
    @LogDataSourceError
    public void deleteAllByIds(List<Long> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("ids is null");
        }
        if (ids.isEmpty()) {
            return;
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("ids has null object");
        }
        repository.deleteAllById(ids);
    }
}
