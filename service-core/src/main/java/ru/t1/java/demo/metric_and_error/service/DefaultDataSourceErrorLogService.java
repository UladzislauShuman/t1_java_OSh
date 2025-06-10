package ru.t1.java.demo.metric_and_error.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import r1.t1.monitoring.starter.annotation.LogDataSourceError;
import r1.t1.monitoring.starter.annotation.Metric;
import r1.t1.monitoring.starter.model.DataSourceErrorLog;
import r1.t1.monitoring.starter.repository.DataSourceErrorLogRepository;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.metric_and_error.dto.DataSourceErrorLogDto;
import ru.t1.java.demo.metric_and_error.util.DataSourceErrorLogMapper;
import ru.t1.java.demo.service.AbstractCrudService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultDataSourceErrorLogService extends AbstractCrudService<DataSourceErrorLog, DataSourceErrorLogDto>
        implements DataSourceErrorLogService {
    private final DataSourceErrorLogRepository repository;


    @Override
    protected CrudRepository<DataSourceErrorLog, Long> getRepository() {
        return repository;
    }

    @Override
    protected Function<DataSourceErrorLog, DataSourceErrorLogDto> toDto() {
        return DataSourceErrorLogMapper::toDto;
    }

    @Override
    protected Function<DataSourceErrorLogDto, DataSourceErrorLog> toEntity() {
        return DataSourceErrorLogMapper::toEntity;
    }

    @Override
    @LogDataSourceError
    @Metric
    public Page<DataSourceErrorLogDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(DataSourceErrorLogMapper::toDto);
    }

    @Override
    @LogDataSourceError
    @Metric
    public Optional<DataSourceErrorLogDto> findById(Long id) {
        return super.findById(id);
    }

    @Override
    @LogDataSourceError
    @Metric
    public List<DataSourceErrorLogDto> findAllByIds(List<Long> ids) {
        return super.findAllByIds(ids);
    }

    @Override
    @LogDataSourceError
    @Metric
    public DataSourceErrorLogDto save(DataSourceErrorLogDto dto) {
        return super.save(dto);
    }

    @Override
    @LogDataSourceError
    @Metric
    public Iterable<DataSourceErrorLogDto> saveAll(Collection<DataSourceErrorLogDto> dtos) {
        return super.saveAll(dtos);
    }

    @Override
    @LogDataSourceError
    @Metric
    public void delete(DataSourceErrorLogDto dto) {
        super.delete(dto);
    }

    @Override
    @LogDataSourceError
    @Metric
    public void deleteAllByIds(List<Long> ids) {
        super.deleteAllByIds(ids);
    }
}