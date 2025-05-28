package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import ru.t1.java.demo.aop.my.Cached;
import ru.t1.java.demo.aop.my.LogDataSourceError;
import ru.t1.java.demo.dto.MetricLogDto;
import ru.t1.java.demo.model.MetricLog;
import ru.t1.java.demo.repository.MetricLogRepository;
import ru.t1.java.demo.util.MetricLogMapper;

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
    @Cached
    public List<MetricLogDto> findAll() {
        Iterable<MetricLog> metricLogs = getRepository().findAll();
        return StreamSupport.stream(metricLogs.spliterator(), false)
                .map(toDto())
                .collect(Collectors.toList());
    }

    @Override
    @LogDataSourceError
    @Cached
    public Page<MetricLogDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(MetricLogMapper::toDto);
    }

    @Override
    @LogDataSourceError
    @Cached
    public Optional<MetricLogDto> findById(Long id) {
        return super.findById(id);
    }

    @Override
    @LogDataSourceError
    @Cached
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