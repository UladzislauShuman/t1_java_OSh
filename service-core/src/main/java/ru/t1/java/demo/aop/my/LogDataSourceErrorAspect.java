package ru.t1.java.demo.aop.my;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.kafka.KafkaProducerService;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.model.MetricLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogDataSourceErrorAspect {
    public static final String PARAMETER_METHOD_SIGNATURE = "methodSignature";
    public static final String PARAMETER_ERROR_MESSAGE = "errorMessage";
    public static final String PARAMETER_EXCEPTION_CLASS = "exceptionClass";
    public static final String PARAMETER_STACK_TRACE = "stackTrace";
    public static final String PARAMETER_TIMESTAMP = "timestamp";
    private final DataSourceErrorLogRepository errorLogRepository;
    private final KafkaProducerService kafkaProducerService;

    public static final String KAFKA_TOPIC_METRICS = "t1_demo_metrics";
    public static final String ERROR_TYPE_DATA_SOURCE = "DATA_SOURCE";

    @AfterThrowing(
            pointcut = "@annotation(ru.t1.java.demo.aop.my.LogDataSourceError)",
            throwing = "exception"
    )
    public void setErrorLogRepository(JoinPoint joinPoint, Throwable exception) {
        String errorMessage = exception.getMessage() != null ? exception.getMessage() : "no message";
        String stackTrace = Arrays.toString(exception.getStackTrace());
        String methodSignature = joinPoint.getSignature().toShortString();

        Map<String, Object> payload = getPayload(methodSignature, errorMessage, exception, stackTrace);
        sendToKafka(payload, methodSignature, errorMessage, stackTrace);

        log.error("Exception added to log FROM {}, INCLUDE: {}",joinPoint.getSignature().toShortString(), exception.getMessage());
    }

    private Map<String, Object> getPayload(
            String methodSignature, String errorMessage, Throwable exception, String stackTrace) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(PARAMETER_METHOD_SIGNATURE, methodSignature);
        payload.put(PARAMETER_ERROR_MESSAGE, errorMessage);
        payload.put(PARAMETER_EXCEPTION_CLASS, exception.getClass().getName());
        payload.put(PARAMETER_STACK_TRACE, stackTrace);
        payload.put(PARAMETER_TIMESTAMP, LocalDateTime.now().toString());

        return payload;
    }

    private void sendToKafka(Map<String, Object> payload,
                             String methodSignature, String errorMessage, String stackTrace) {
        boolean sentToKafka = false;
        try {
            sentToKafka = kafkaProducerService.sendMessage(
                    KAFKA_TOPIC_METRICS,
                    payload,
                    ERROR_TYPE_DATA_SOURCE
            );
        } catch (Exception e) {
            log.error("something goes wrong while trying to send LogDataSource to Kafka method={}, exception={}",
                    methodSignature, e.getMessage());
            sentToKafka = false;
        }

        if (sentToKafka == false) {
            log.error("error in process of sending method {} LogDataSource to Kafka", methodSignature);
            log.error("send it to DB");
            DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                    .message(errorMessage)
                    .stackTrace(stackTrace)
                    .methodSignature(methodSignature)
                    .build();
            errorLogRepository.save(errorLog);
        } else {
            log.error("LogDataSource of method {} sent to Kafka successfully", methodSignature);
        }
    }
}
