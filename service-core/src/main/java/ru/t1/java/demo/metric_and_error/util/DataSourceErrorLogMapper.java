package ru.t1.java.demo.metric_and_error.util;

import org.springframework.stereotype.Component;
import r1.t1.monitoring.starter.model.DataSourceErrorLog;
import ru.t1.java.demo.metric_and_error.dto.DataSourceErrorLogDto;

@Component
public class DataSourceErrorLogMapper {
    public static DataSourceErrorLog toEntity(DataSourceErrorLogDto dto) {
        return DataSourceErrorLog.builder()
                .stackTrace(dto.getStackTrace())
                .message(dto.getMessage())
                .methodSignature(dto.getMethodSignature())
                .build();
    }

    public static DataSourceErrorLogDto toDto(DataSourceErrorLog entity) {
        return DataSourceErrorLogDto.builder()
                .id(entity.getId())
                .stackTrace(entity.getStackTrace())
                .message(entity.getMessage())
                .methodSignature(entity.getMethodSignature())
                .build();
    }
}