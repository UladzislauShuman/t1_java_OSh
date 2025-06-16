package ru.t1.java.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.Client;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {
    Optional<Client> findByClientId(UUID clientId);
    Page<Client> findByStatus(Client.Status status, Pageable pageable);
    long countByStatus(Client.Status status);
}