package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.t1.java.demo.model.MetricLog;

import java.time.LocalDateTime;

/**
 * DTO for {@link MetricLog}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricLogDto {
    private Long id;

    @JsonProperty("method_signature")
    private String methodSignature;

    @JsonProperty("execution_time_ms")
    private long executionTimeMs;

    @JsonProperty("limit_ms")
    private long limitMs;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}