package ru.t1.java.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.Account;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
    Page<Account> findAll(Pageable pageable);
    Optional<Account> findByAccountId(UUID accountId);
}