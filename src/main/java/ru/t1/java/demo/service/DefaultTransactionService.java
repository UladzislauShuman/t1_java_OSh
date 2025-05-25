package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.util.TransactionMapper;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultTransactionService extends AbstractCrudService<Transaction, TransactionDto>
                                        implements TransactionService {
    private final TransactionRepository repository;

    @Override
    public CrudRepository<Transaction, Long> getRepository() {
        return repository;
    }

    @Override
    public Function<Transaction, TransactionDto> toDto() {
        return TransactionMapper::toDto;
    }

    @Override
    public Function<TransactionDto, Transaction> toEntity() {
        return TransactionMapper::toEntity;
    }

    @Override
    public Page<TransactionDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(TransactionMapper::toDto);
    }
}
