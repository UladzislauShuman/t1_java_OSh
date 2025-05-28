package ru.t1.java.demo.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.Client;

@Repository
public interface ClientRepository extends CrudRepository<Client, Long> { }