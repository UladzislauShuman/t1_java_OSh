package ru.t1.java.demo.aop.my;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.service.CacheAspectService;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.StringJoiner;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class CachedAspect {
    public static final String CACHE_FOR_METHOD_BY_KEY = "cache for method {} by key {}";
    public static final String NO_CACHE_FOR_METHOD_BY_KEY = "no cache for method {} by key {}";
    public static final String CACHED_FOR_BY_KEY = "cached for {} by key {}";
    public static final String NOT_CACHED_FOR_METHOD = "didnt cache {}, because method returned null";
    public static final String NULL = "null";
    public static final String NO_ARGS = "no_args";
    public static final String GENERATE_KEY_FOR = "generate key for {} : {}";
    public static final String CACHED_LIVE_LIMIT = "${app.cached.live-limit:60000}";
    public static final String CACHED = "@annotation(ru.t1.java.demo.aop.my.Cached)";

    private final CacheAspectService service;

    @Value(CACHED_LIVE_LIMIT)
    private long liveLimit;

    @Around(CACHED)
    public Object cacheMethodResult(ProceedingJoinPoint joinPoint) throws Throwable {
        String cacheKey = generateCacheKey(joinPoint);
        Optional<Object> cachedResult = service.get(cacheKey);
        if (cachedResult.isPresent()) {
            log.error(CACHE_FOR_METHOD_BY_KEY, joinPoint.getSignature().toShortString(), cacheKey);
            return cachedResult.get();
        } else {
            log.error(NO_CACHE_FOR_METHOD_BY_KEY, joinPoint.getSignature().toShortString(), cacheKey);
            Object result = proceedJoinPoint(joinPoint, cacheKey);
            return result;
        }
    }

    private Object proceedJoinPoint(ProceedingJoinPoint joinPoint, String cacheKey) throws Throwable {
        Object result = joinPoint.proceed();
        if (result != null) {
            service.put(cacheKey, result);
            log.error(CACHED_FOR_BY_KEY, joinPoint.getSignature().toShortString(), cacheKey);
        } else {
            log.error(NOT_CACHED_FOR_METHOD, joinPoint.getSignature().toShortString());
        }
        return result;
    }

    // я думаю ключ с такой структурой будет однозначно определяться и не будет
    // коллизии по причине (я не отменяю другие причины) одинаковости имени(ну т.е. ключа)
    // <полное имя класса>
    //  .<метод>:
    //      <аргумент 1>,
    //      ...,
    //      <аргумент К>
    // если нет аргументов -- NO_ARGS
    private String generateCacheKey(ProceedingJoinPoint joinPoint) {
        String key = getClassName(joinPoint) +
                    "." +
                    getMethodName(joinPoint) +
                    getArgs(joinPoint);

        log.error(GENERATE_KEY_FOR, joinPoint.getSignature().toShortString(), key);
        return key;
    }

    private String getClassName(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        return method.getDeclaringClass().getName();
    }

    private String getMethodName(ProceedingJoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
    }

    private String getArgs(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        StringJoiner stringJoiner = new StringJoiner(",", ":", "");

        if (args != null && args.length > 0)
            for (Object arg : args)
                stringJoiner.add(arg == null ? NULL : arg.toString());
        else
            stringJoiner.add(NO_ARGS);

        return stringJoiner.toString();
    }
}
