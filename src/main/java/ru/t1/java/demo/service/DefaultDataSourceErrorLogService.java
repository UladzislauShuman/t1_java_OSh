package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.aop.my.LogDataSourceError;
import ru.t1.java.demo.dto.DataSourceErrorLogDto;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;
import ru.t1.java.demo.util.DataSourceErrorLogMapper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor
public class DefaultDataSourceErrorLogService implements  DataSourceErrorLogService{
    private final DataSourceErrorLogRepository repository;

    @Override
    @LogDataSourceError
    public Page<DataSourceErrorLogDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(DataSourceErrorLogMapper::toDto);
    }

    @Override
    @LogDataSourceError
    public Optional<DataSourceErrorLogDto> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id == null");
        }
        DataSourceErrorLog logEntry = repository.findById(id).orElse(null);
        return Optional.ofNullable(DataSourceErrorLogMapper.toDto(logEntry));
    }

    @Override
    @LogDataSourceError
    public List<DataSourceErrorLogDto> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        Iterable<DataSourceErrorLog> logEntries = repository.findAllById(ids);
        return StreamSupport.stream(logEntries.spliterator(), false)
                .map(DataSourceErrorLogMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @LogDataSourceError
    public DataSourceErrorLogDto save(DataSourceErrorLogDto logDto) {
        if (logDto == null) {
            throw new IllegalArgumentException("logDto == null");
        }
        DataSourceErrorLog logEntry = DataSourceErrorLogMapper.toEntity(logDto);
        repository.save(logEntry);
        return logDto;
    }

    @Override
    @LogDataSourceError
    public Iterable<DataSourceErrorLogDto> saveAll(Collection<DataSourceErrorLogDto> logDtos) {
        if (logDtos == null || logDtos.isEmpty()) {
            return Collections.emptyList();
        }
        if (logDtos.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("logDtos has null object");
        }

        List<DataSourceErrorLog> logEntries = (List<DataSourceErrorLog>) repository.saveAll(logDtos.stream()
                .map(DataSourceErrorLogMapper::toEntity)
                .collect(Collectors.toList()));
        return logEntries.stream()
                .map(DataSourceErrorLogMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @LogDataSourceError
    public void delete(DataSourceErrorLogDto logDto) {
        if (logDto == null) {
            throw new IllegalArgumentException("logDto is null");
        }
        Optional<DataSourceErrorLog> logEntry = repository.findById(DataSourceErrorLogMapper.toEntity(logDto).getId());
        if (logEntry.isPresent()) {
            repository.delete(logEntry.get());
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