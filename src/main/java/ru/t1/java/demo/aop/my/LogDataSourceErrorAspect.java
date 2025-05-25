package ru.t1.java.demo.aop.my;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogDataSourceErrorAspect {
    private final DataSourceErrorLogRepository errorLogRepository;

    @AfterThrowing(
            pointcut = "@annotation(ru.t1.java.demo.aop.my.LogDataSourceError)",
            throwing = "exception"
    )
    public void setErrorLogRepository(JoinPoint joinPoint, Throwable exception) {
        String errorMessage = exception.getMessage() != null ? exception.getMessage() : "no message";
        String stackTrace = Arrays.toString(exception.getStackTrace());
        String methodSignature = joinPoint.getSignature().toShortString();
        DataSourceErrorLog errorLog = DataSourceErrorLog.builder()
                        .message(errorMessage)
                        .stackTrace(stackTrace)
                        .methodSignature(methodSignature)
                        .build();

        errorLogRepository.save(errorLog);

        log.error("Exception added to log FROM {}, INCLUDE: {}",joinPoint.getSignature().toShortString(), exception.getMessage());
    }
}
