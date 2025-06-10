package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import r1.t1.monitoring.starter.annotation.LogDataSourceError;
import r1.t1.monitoring.starter.annotation.Metric;
import ru.t1.java.demo.aop.my.Cached;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.metric_and_error.dto.DataSourceErrorLogDto;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.util.TransactionMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultTransactionService extends AbstractCrudService<Transaction, TransactionDto>
        implements TransactionService {
    private final TransactionRepository repository;

    @Override
    protected CrudRepository<Transaction, Long> getRepository() {
        return repository;
    }

    @Override
    protected Function<Transaction, TransactionDto> toDto() {
        return TransactionMapper::toDto;
    }

    @Override
    protected Function<TransactionDto, Transaction> toEntity() {
        return TransactionMapper::toEntity;
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
    public Page<TransactionDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(TransactionMapper::toDto);
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
    public Optional<TransactionDto> findById(Long id) {
        return super.findById(id);
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
    public List<TransactionDto> findAllByIds(List<Long> ids) {
        return super.findAllByIds(ids);
    }

    @Override
    @LogDataSourceError
    @Metric
    public TransactionDto save(TransactionDto dto) {
        return super.save(dto);
    }

    @Override
    @LogDataSourceError
    @Metric
    public Iterable<TransactionDto> saveAll(Collection<TransactionDto> dtos) {
        return super.saveAll(dtos);
    }

    @Override
    @LogDataSourceError
    @Metric
    public void delete(TransactionDto dto) {
        super.delete(dto);
    }

    @Override
    @LogDataSourceError
    @Metric
    public void deleteAllByIds(List<Long> ids) {
        super.deleteAllByIds(ids);
    }
}