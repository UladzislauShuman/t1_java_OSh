package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.AccountDto;

import java.util.UUID;

public interface AccountService extends CrudService<AccountDto> {
    AccountDto findByAccountId(UUID accountId);
}

