package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.MetricLogDto;

import java.util.List;

public interface MetricLogService extends CrudService<MetricLogDto> {
    List<MetricLogDto> findAll();
}
