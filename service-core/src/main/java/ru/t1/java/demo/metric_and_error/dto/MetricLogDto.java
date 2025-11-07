package ru.t1.java.demo.metric_and_error.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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