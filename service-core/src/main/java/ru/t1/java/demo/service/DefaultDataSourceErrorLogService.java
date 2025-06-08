package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.aop.my.Cached;
import ru.t1.java.demo.aop.my.LogDataSourceError;
import ru.t1.java.demo.aop.my.Metric;
import ru.t1.java.demo.dto.DataSourceErrorLogDto;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;
import ru.t1.java.demo.util.DataSourceErrorLogMapper;

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
    @Cached
    public Page<DataSourceErrorLogDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(DataSourceErrorLogMapper::toDto);
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
    public Optional<DataSourceErrorLogDto> findById(Long id) {
        return super.findById(id);
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
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