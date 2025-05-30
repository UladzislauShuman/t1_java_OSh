package ru.t1.java.demo.util;

import org.springframework.stereotype.Component;
import ru.t1.java.demo.dto.MetricLogDto;
import ru.t1.java.demo.model.MetricLog;


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


