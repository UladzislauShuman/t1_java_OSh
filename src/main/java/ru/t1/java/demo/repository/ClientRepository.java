package ru.t1.java.demo.repository;

import org.springframework.data.repository.CrudRepository;
import ru.t1.java.demo.model.Client;

public interface ClientRepository extends CrudRepository<Client, Long> { }