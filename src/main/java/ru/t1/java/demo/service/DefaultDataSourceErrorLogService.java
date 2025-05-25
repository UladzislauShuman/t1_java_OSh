package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.dto.DataSourceErrorLogDto;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;
import ru.t1.java.demo.util.DataSourceErrorLogMapper;

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
    public Page<DataSourceErrorLogDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(DataSourceErrorLogMapper::toDto);
    }
}