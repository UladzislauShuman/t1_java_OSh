package ru.t1.java.demo.aop.my;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.java.demo.service.CacheAspectService;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachedAspectTest {
    @Mock
    private CacheAspectService cacheAspectService;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private CachedAspect cachedAspect;

    public void methodWithArgs(String text, Integer number, UUID id) {}
    public void methodWithNoArgs() {}

    @Test
    void generateCorrectKeyForArgMethod() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("methodWithArgs", String.class, Integer.class, UUID.class);

        UUID testId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Object[] args = {"text", 123, testId};

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(methodSignature.getMethod()).thenReturn(method);

        String expectedKey = "ru.t1.java.demo.aop.my.CachedAspectTest.methodWithArgs:text,123,123e4567-e89b-12d3-a456-426614174000";

        String actualKey = ReflectionTestUtils.invokeMethod(cachedAspect, "generateCacheKey", joinPoint);

        assertEquals(expectedKey, actualKey);
    }

    @Test
    void generateCorrectKeyForNoArgMethod() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("methodWithNoArgs");

        Object[] args = {};

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(methodSignature.getMethod()).thenReturn(method);

        String expectedKey = "ru.t1.java.demo.aop.my.CachedAspectTest.methodWithNoArgs:no_args";

        String actualKey = ReflectionTestUtils.invokeMethod(cachedAspect, "generateCacheKey", joinPoint);

        assertEquals(expectedKey, actualKey);
    }

    @Test
    void generateCorrectKeyForArgMethodWithNull() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("methodWithArgs", String.class, Integer.class, UUID.class);
        Object[] args = {"text", null, null};

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(methodSignature.getMethod()).thenReturn(method);

        String expectedKey = "ru.t1.java.demo.aop.my.CachedAspectTest.methodWithArgs:text,null,null";

        String actualKey = ReflectionTestUtils.invokeMethod(cachedAspect, "generateCacheKey", joinPoint);

        assertEquals(expectedKey, actualKey);
    }

}