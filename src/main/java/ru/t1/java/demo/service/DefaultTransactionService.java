package ru.t1.java.demo.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.util.TransactionMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultTransactionService implements TransactionService {
    private final TransactionRepository repository;

    @Override
    public Page<TransactionDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(TransactionMapper::toDto);
    }

    @Override
    public Optional<TransactionDto> findById(Long id) {
        // проверка
        Transaction transaction = repository.findById(id).orElse(null); // исключение?
        return Optional.ofNullable(TransactionMapper.toDto(transaction));
    }

    @Override
    public List<TransactionDto> findAllByIds(List<Long> ids) {
        Iterable<Transaction> transactions = repository.findAllById(ids);
        return StreamSupport.stream(transactions.spliterator(), false)
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDto save(TransactionDto transactionDto) {
        // проверки и другой сценарий
        Transaction transaction = TransactionMapper.toEntity(transactionDto);
        repository.save(transaction);
        return transactionDto;
    }

    @Override
    public Iterable<TransactionDto> saveAll(Collection<TransactionDto> transactionDtos) {
        // проверки
        List<Transaction> transactions = (List<Transaction>) repository.saveAll(transactionDtos.stream()
                .map(TransactionMapper::toEntity)
                .collect(Collectors.toList()));
        return transactions.stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(TransactionDto transactionDto) {
        // проверки
        Optional<Transaction> transaction = repository.findById(TransactionMapper.toEntity(transactionDto).getId());
        if (transaction.isPresent()) {
            repository.delete(transaction.get());
        }
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        // проверки
        repository.deleteAllById(ids);
    }
}
