package ru.t1.java.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.t1.java.demo.dto.DataSourceErrorLogDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DataSourceErrorLogService {
    Page<DataSourceErrorLogDto> findAll(Pageable pageable);
    Optional<DataSourceErrorLogDto> findById(Long id);
    List<DataSourceErrorLogDto> findAllByIds(List<Long> ids);
    DataSourceErrorLogDto save(DataSourceErrorLogDto transaction);
    Iterable<DataSourceErrorLogDto> saveAll(Collection<DataSourceErrorLogDto> transactions);
    void delete(DataSourceErrorLogDto transaction);
    void deleteAllByIds(List<Long> ids);
}
