package ru.t1.java.demo.metric_and_error.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import r1.t1.monitoring.starter.annotation.LogDataSourceError;
import r1.t1.monitoring.starter.annotation.Metric;
import ru.t1.java.demo.metric_and_error.dto.DataSourceErrorLogDto;
import ru.t1.java.demo.metric_and_error.service.DataSourceErrorLogService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/data-source-error-logs")
@RequiredArgsConstructor
public class DataSourceErrorLogController {

    private final DataSourceErrorLogService errorLogService;

    @GetMapping("/exception")
    @LogDataSourceError
    @Metric
    public void throwTestException() {
        throw new RuntimeException("test of LogDataSourceErrorAspect");
    }

    @GetMapping
    @Metric
    public PagedModel<DataSourceErrorLogDto> getAll(Pageable pageable) {
        Page<DataSourceErrorLogDto> errorLogs = errorLogService.findAll(pageable);
        return new PagedModel<>(errorLogs);
    }

    @GetMapping("/{id}")
    @Metric
    public DataSourceErrorLogDto getOne(@PathVariable Long id) {
        Optional<DataSourceErrorLogDto> errorLogOptional = errorLogService.findById(id);
        return errorLogOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @GetMapping("/by-ids")
    @Metric
    public Iterable<DataSourceErrorLogDto> getMany(@RequestParam List<Long> ids) {
        return errorLogService.findAllByIds(ids);
    }
}
