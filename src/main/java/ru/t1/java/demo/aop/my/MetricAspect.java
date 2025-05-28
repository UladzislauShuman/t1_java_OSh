package ru.t1.java.demo.aop.my;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.MetricLog;
import ru.t1.java.demo.repository.MetricLogRepository;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MetricAspect {

    public static final String METHOD_COMPLETED_IN = "Method {} completed in {}";
    public static final String METHOD_HAS_EXCEEDED_ITS_LIMIT = "Method {} has exceeded its limit, It: {}, and Limit: {}";

    private final MetricLogRepository repository;

    @Value("${app.constants.metric.execution-time-limit:100}")
    private long EXECUTION_TIME_LIMITS_MS;

    @Around("@annotation(ru.t1.java.demo.aop.my.Metric)")
    public Object getMetric(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        Object result;

        try {
            result = joinPoint.proceed();
        } finally {
            long endTime = System.nanoTime();
            long durationMs = getDurationMs(startTime, endTime);
            String methodSignature = joinPoint.getSignature().toShortString();
            log.error(METHOD_COMPLETED_IN, methodSignature, durationMs);

            if (isInLimits(durationMs)) {
                log.error(METHOD_HAS_EXCEEDED_ITS_LIMIT,
                        methodSignature, durationMs, EXECUTION_TIME_LIMITS_MS);
                MetricLog metricLog = getMetricLog(methodSignature, durationMs);
                saveMetricLog(metricLog);
            }
        }
        return result;
    }

    private long getDurationMs(long start, long end) {
        return (end - start) / 1_000_000;
    }

    private MetricLog getMetricLog(String methodSignature, long duration) {
        return MetricLog.builder()
                .methodSignature(methodSignature)
                .executionTimeMs(duration)
                .limitMs(EXECUTION_TIME_LIMITS_MS)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private boolean isInLimits(long duration) {
        return duration > EXECUTION_TIME_LIMITS_MS;
    }

    private void saveMetricLog(MetricLog metricLog) {
        try {
            repository.save(metricLog);
        } catch (Exception e) {
            log.error("Ошибка: не получилось сохранить в репозиторий MetricLogRepository");
        }
    }
}
