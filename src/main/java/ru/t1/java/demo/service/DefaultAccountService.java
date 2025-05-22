package ru.t1.java.demo.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.util.AccountMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultAccountService implements AccountService {
    private final AccountRepository repository;

    @Override
    public Page<AccountDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(AccountMapper::toDto);
    }

    @Override
    public Optional<AccountDto> findById(Long id) {
        Account account = repository.findById(id).orElse(null);
        return Optional.ofNullable(AccountMapper.toDto(account));
    }

    @Override
    public List<AccountDto> findAllByIds(List<Long> ids) {
        Iterable<Account> accounts = repository.findAllById(ids);
        return StreamSupport.stream(accounts.spliterator(), false)
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDto save(AccountDto accountDto) {
        Account account = AccountMapper.toEntity(accountDto);
        repository.save(account);
        return accountDto;
    }

    @Override
    public Iterable<AccountDto> saveAll(Collection<AccountDto> accountDtos) {
        List<Account> accounts = (List<Account>) repository.saveAll(accountDtos.stream()
                .map(AccountMapper::toEntity)
                .collect(Collectors.toList()));
        return accounts.stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(AccountDto accountDto) {
        Optional<Account> account = repository.findById(AccountMapper.toEntity(accountDto).getId());
        account.ifPresent(repository::delete);
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        repository.deleteAllById(ids);
    }
}
