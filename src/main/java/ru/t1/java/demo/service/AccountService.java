package ru.t1.java.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.t1.java.demo.dto.AccountDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    Page<AccountDto> findAll(Pageable pageable);
    Optional<AccountDto> findById(Long id);
    List<AccountDto> findAllByIds(List<Long> ids);
    AccountDto save(AccountDto account);
    Iterable<AccountDto> saveAll(Collection<AccountDto> accounts);
    void delete(AccountDto account);
    void deleteAllByIds(List<Long> ids);
}

