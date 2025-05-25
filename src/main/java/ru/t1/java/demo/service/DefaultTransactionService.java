package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.aop.my.LogDataSourceError;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.util.TransactionMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultTransactionService implements TransactionService {
    private final TransactionRepository repository;

    @Override
    @LogDataSourceError
    public Page<TransactionDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(TransactionMapper::toDto);
    }

    @Override
    @LogDataSourceError
    public Optional<TransactionDto> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id == null");
        }
        Transaction transaction = repository.findById(id).orElse(null); // исключение?
        return Optional.ofNullable(TransactionMapper.toDto(transaction));
    }

    @Override
    @LogDataSourceError
    public List<TransactionDto> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        Iterable<Transaction> transactions = repository.findAllById(ids);
        return StreamSupport.stream(transactions.spliterator(), false)
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @LogDataSourceError
    public TransactionDto save(TransactionDto transactionDto) {
        if (transactionDto == null) {
            throw new IllegalArgumentException("transactionDto == null");
        }
        Transaction transaction = TransactionMapper.toEntity(transactionDto);
        repository.save(transaction);
        return transactionDto;
    }

    @Override
    @LogDataSourceError
    public Iterable<TransactionDto> saveAll(Collection<TransactionDto> transactionDtos) {
        if (transactionDtos == null || transactionDtos.isEmpty()) {
            return Collections.emptyList();
        }
        if (transactionDtos.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("transactionDtos has null object");
        }

        List<Transaction> transactions = (List<Transaction>) repository.saveAll(transactionDtos.stream()
                .map(TransactionMapper::toEntity)
                .collect(Collectors.toList()));
        return transactions.stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @LogDataSourceError
    public void delete(TransactionDto transactionDto) {
       if (transactionDto == null) {
           throw new IllegalArgumentException("transactionDto is null");
       }
        Optional<Transaction> transaction = repository.findById(TransactionMapper.toEntity(transactionDto).getId());
        if (transaction.isPresent()) {
            repository.delete(transaction.get());
        }
    }

    @Override
    @LogDataSourceError
    public void deleteAllByIds(List<Long> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("ids is null");
        }
        if (ids.isEmpty()) {
            return;
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("ids has null object");
        }
        repository.deleteAllById(ids);
    }
}
