package ru.t1.java.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.t1.java.demo.dto.TransactionDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Page<TransactionDto> findAll(Pageable pageable);
    Optional<TransactionDto> findById(Long id);
    List<TransactionDto> findAllByIds(List<Long> ids);
    TransactionDto save(TransactionDto transaction);
    Iterable<TransactionDto> saveAll(Collection<TransactionDto> transactions);
    void delete(TransactionDto transaction);
    void deleteAllByIds(List<Long> ids);
}
