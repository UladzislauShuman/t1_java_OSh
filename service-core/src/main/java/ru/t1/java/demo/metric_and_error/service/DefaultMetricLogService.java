package ru.t1.java.demo.metric_and_error.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import r1.t1.monitoring.starter.annotation.LogDataSourceError;
import r1.t1.monitoring.starter.model.MetricLog;
import r1.t1.monitoring.starter.repository.MetricLogRepository;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.metric_and_error.dto.MetricLogDto;
import ru.t1.java.demo.metric_and_error.util.MetricLogMapper;
import ru.t1.java.demo.service.AbstractCrudService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultMetricLogService extends AbstractCrudService<MetricLog, MetricLogDto> implements MetricLogService {

    private final MetricLogRepository repository;

    @Override
    protected CrudRepository<MetricLog, Long> getRepository() {
        return repository;
    }

    @Override
    protected Function<MetricLog, MetricLogDto> toDto() {
        return MetricLogMapper::toDto;
    }

    @Override
    protected Function<MetricLogDto, MetricLog> toEntity() {
        return MetricLogMapper::toEntity;
    }

    @Override
    @LogDataSourceError
    public List<MetricLogDto> findAll() {
        Iterable<MetricLog> metricLogs = getRepository().findAll();
        return StreamSupport.stream(metricLogs.spliterator(), false)
                .map(toDto())
                .collect(Collectors.toList());
    }

    @Override
    @LogDataSourceError
    public Page<MetricLogDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(MetricLogMapper::toDto);
    }

    @Override
    @LogDataSourceError
    public Optional<MetricLogDto> findById(Long id) {
        return super.findById(id);
    }

    @Override
    @LogDataSourceError
    public List<MetricLogDto> findAllByIds(List<Long> ids) {
        return super.findAllByIds(ids);
    }

    @Override
    @LogDataSourceError
    public MetricLogDto save(MetricLogDto dto) {
        return super.save(dto);
    }

    @Override
    @LogDataSourceError
    public Iterable<MetricLogDto> saveAll(Collection<MetricLogDto> dtos) {
        return super.saveAll(dtos);
    }

    @Override
    @LogDataSourceError
    public void delete(MetricLogDto dto) {
        super.delete(dto);
    }

    @Override
    @LogDataSourceError
    public void deleteAllByIds(List<Long> ids) {
        super.deleteAllByIds(ids);
    }
}