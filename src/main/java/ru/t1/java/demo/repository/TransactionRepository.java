package ru.t1.java.demo.repository;

import org.springframework.data.repository.CrudRepository;
import ru.t1.java.demo.model.Transaction;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {}
