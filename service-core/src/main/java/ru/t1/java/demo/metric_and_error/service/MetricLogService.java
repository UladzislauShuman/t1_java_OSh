package ru.t1.java.demo.metric_and_error.service;

import ru.t1.java.demo.metric_and_error.dto.MetricLogDto;
import ru.t1.java.demo.service.CrudService;

import java.util.List;

public interface MetricLogService extends CrudService<MetricLogDto> {
    List<MetricLogDto> findAll();
}
