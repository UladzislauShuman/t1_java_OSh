package ru.t1.java.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.java.demo.aop.my.CachedAspect;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CacheAspectServiceTest {
    private CacheAspectService cacheAspectService;
    private final long LIVE_LIMIT_MS = 1000;

    @BeforeEach
    void setUp() {
        cacheAspectService = new CacheAspectService();
        ReflectionTestUtils.setField(cacheAspectService, "liveLimit", LIVE_LIMIT_MS);
    }

    private Map<String, CachedAspect> getInternalCache() {
        return (Map<String, CachedAspect>) ReflectionTestUtils.getField(cacheAspectService, "cache");
    }

    @Test
    void putAndGetSuccessfully() {
        String key = "test_key";
        String value = "test_value";

        cacheAspectService.put(key, value);
        Optional<Object> result = cacheAspectService.get(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
        assertEquals(1, getInternalCache().size());
    }

    @Test
    void returnEmptyForNonExistentKey() {
        String key = "test_key";

        Optional<Object> result = cacheAspectService.get(key);

        assertFalse(result.isPresent());
        assertTrue(getInternalCache().isEmpty());
    }

    @Test
    void notReturnExpiredValueAndRemoveIt() throws InterruptedException {
        String key = "test_key";
        String value = "test_value";
        cacheAspectService.put(key, value);

        Thread.sleep(LIVE_LIMIT_MS + 100);

        Optional<Object> result = cacheAspectService.get(key);

        assertFalse(result.isPresent());
        assertTrue(getInternalCache().isEmpty());
    }

    @Test
    void removeExpiredKeysShouldOnlyRemoveExpiredEntries() throws InterruptedException {
        String expiredKey = "expired";
        String freshKey = "fresh";

        cacheAspectService.put(expiredKey, "expire");
        Thread.sleep(LIVE_LIMIT_MS + 100);
        cacheAspectService.put(freshKey, "freshValue");
        assertEquals(2, getInternalCache().size());
        cacheAspectService.removeExpiredKeys();

        Map<String, CachedAspect> cache = getInternalCache();
        assertEquals(1, cache.size());
        assertTrue(cache.containsKey(freshKey));
        assertFalse(cache.containsKey(expiredKey));
    }

    @Test
    void clearRemoveAllEntries() {
        cacheAspectService.put("key1", "value1");
        cacheAspectService.put("key2", "value2");
        assertEquals(2, getInternalCache().size());

        cacheAspectService.clear();

        assertTrue(getInternalCache().isEmpty());
    }
}