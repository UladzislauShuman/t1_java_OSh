package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSourceErrorLogDto implements Serializable{
    private Long id;
    @JsonProperty("stack_trace")
    private String stackTrace;
    @JsonProperty("message")
    private String message;
    @JsonProperty("method_signature")
    private String methodSignature;
}