package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.t1.java.demo.aop.my.Metric;
import ru.t1.java.demo.dto.DataSourceErrorLogDto;
import ru.t1.java.demo.service.DataSourceErrorLogService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/data-source-error-logs")
@RequiredArgsConstructor
public class DataSourceErrorLogController {

    private final DataSourceErrorLogService errorLogService;

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
