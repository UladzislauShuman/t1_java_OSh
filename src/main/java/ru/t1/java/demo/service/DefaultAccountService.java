package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.util.AccountMapper;

import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultAccountService extends AbstractCrudService<Account, AccountDto>
                                    implements AccountService {
    private final AccountRepository repository;

    @Override
    public CrudRepository<Account, Long> getRepository() {
        return repository;
    }

    @Override
    public Function<Account, AccountDto> toDto() {
        return AccountMapper::toDto;
    }

    @Override
    public Function<AccountDto, Account> toEntity() {
        return AccountMapper::toEntity;
    }

    @Override
    public Page<AccountDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(AccountMapper::toDto);
    }
}
