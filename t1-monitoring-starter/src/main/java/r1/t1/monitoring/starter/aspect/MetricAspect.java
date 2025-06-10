package r1.t1.monitoring.starter.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import r1.t1.monitoring.starter.kafka.MonitoringKafkaProducerService;
import r1.t1.monitoring.starter.model.MetricLog;
import r1.t1.monitoring.starter.repository.MetricLogRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Slf4j
@RequiredArgsConstructor
public class MetricAspect {

    public static final String METHOD_COMPLETED_IN = "Method {} completed in {}";
    public static final String METHOD_HAS_EXCEEDED_ITS_LIMIT = "Method {} has exceeded its limit, It: {}, and Limit: {}";

    //public static final String KAFKA_TOPIC_METRICS = "t1_demo_metrics";
    public static final String ERROR_TYPE_METRICS = "METRICS";

    public static final String PARAMETER_METHOD_SIGNATURE = "methodSignature";
    public static final String PARAMETER_EXECUTION_TIME = "executionTime";
    public static final String PARAMETER_LIMIT_MS = "limitMs";
    public static final String PARAMETER_TIMESTAMP = "timestamp";

    private final MetricLogRepository repository;
    private final MonitoringKafkaProducerService kafkaProducerService;
    private final long executionTimeLimitMs;
    private final String kafkaTopic;

    @Around("@annotation(r1.t1.monitoring.starter.annotation.Metric)")
    public Object getMetric(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        Object result;

        try {
            result = joinPoint.proceed();
        } finally {
            long endTime = System.nanoTime();
            long executionTime = getDurationMs(startTime, endTime);
            String methodSignature = joinPoint.getSignature().toShortString();
            log.error(METHOD_COMPLETED_IN, methodSignature, executionTime);

            if (isInLimits(executionTime)) {
                log.error(METHOD_HAS_EXCEEDED_ITS_LIMIT,
                        methodSignature, executionTime, executionTimeLimitMs);
                Map<String, Object> payload = getPayload(methodSignature, executionTime);
                sendMessageKafka(payload, methodSignature, executionTime);
            }
        }
        return result;
    }

    private long getDurationMs(long start, long end) {
        return (end - start) / 1_000_000;
    }

    private Map<String , Object> getPayload(String methodSignature, long executionTime) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(PARAMETER_METHOD_SIGNATURE, methodSignature);
        payload.put(PARAMETER_EXECUTION_TIME, executionTime);
        payload.put(PARAMETER_LIMIT_MS,executionTimeLimitMs);
        payload.put(PARAMETER_TIMESTAMP, LocalDateTime.now().toString());
        return payload;
    }

    private void sendMessageKafka(Map<String, Object> payload, String methodSignature, long executionTime) {
        boolean sentToKafka = false;
        try {
            sentToKafka = kafkaProducerService.sendMessage(
                    kafkaTopic,
                    payload,
                    ERROR_TYPE_METRICS
            );
        } catch (Exception exception) {
            log.error("something goes wrong while trying to send metrics to Kafka method={}, exception={}",
                    methodSignature, exception.getMessage());
            sentToKafka = false;
        }

        if (sentToKafka == false) {
            log.error("error in process of sending method {} metrics to Kafka", methodSignature);
            log.error("send it to DB");
            MetricLog metricLog = getMetricLog(methodSignature, executionTime);
            saveMetricLog(metricLog);
        } else {
            log.error("Metrics of method {} sent to Kafka successfully", methodSignature);
        }
    }

    private MetricLog getMetricLog(String methodSignature, long duration) {
        return MetricLog.builder()
                .methodSignature(methodSignature)
                .executionTimeMs(duration)
                .limitMs(executionTimeLimitMs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private boolean isInLimits(long duration) {
        return duration > executionTimeLimitMs;
    }

    private void saveMetricLog(MetricLog metricLog) {
        try {
            repository.save(metricLog);
        } catch (Exception e) {
            log.error("ERROR: failed to save to MetricLogRepository repositories");
        }
    }
}
