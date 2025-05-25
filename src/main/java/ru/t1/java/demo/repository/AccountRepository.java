package ru.t1.java.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import ru.t1.java.demo.model.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Page<Account> findAll(Pageable pageable);
}