package ru.t1.java.demo.metric_and_error.util;

import org.springframework.stereotype.Component;
import r1.t1.monitoring.starter.model.MetricLog;
import ru.t1.java.demo.metric_and_error.dto.MetricLogDto;

@Component
public class MetricLogMapper {
    public static MetricLog toEntity(MetricLogDto dto) {
        return MetricLog.builder()
                .methodSignature(dto.getMethodSignature())
                .executionTimeMs(dto.getExecutionTimeMs())
                .limitMs(dto.getLimitMs())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public static MetricLogDto toDto(MetricLog entity) {
        return MetricLogDto.builder()
                .id(entity.getId())
                .methodSignature(entity.getMethodSignature())
                .executionTimeMs(entity.getExecutionTimeMs())
                .limitMs(entity.getLimitMs())
                .timestamp(entity.getTimestamp())
                .build();
    }
}


