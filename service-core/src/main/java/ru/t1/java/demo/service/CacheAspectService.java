package ru.t1.java.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.CachedObject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@EnableScheduling
public class CacheAspectService {

    private final Map<String, CachedObject> cache = new ConcurrentHashMap<>();

    @Value("${app.cached.live-limit:60000}")
    private long liveLimit;

    @Value("${app.cached.scanning-interval:30000}")
    private long scanningInterval;

    public Optional<Object> get(String key) {
        CachedObject cachedObject = cache.get(key);
        if (cachedObject != null) {
            if (cachedObject.isExpired()) {
                cache.remove(key, cachedObject);
                log.error("удалили Cache по ключу {}", key);
                return Optional.empty();
            }
            return Optional.ofNullable(cachedObject.getValue());
        }
        return Optional.empty();
    }

    public void put(String key, Object value) {
        if (key == null || value == null) {
            log.error("key или value -- null");
            return;
        }
        long expireTIme = System.currentTimeMillis() + liveLimit;
        cache.put(key, new CachedObject(value, expireTIme));
        log.error("за кешировали {} : {}", key, value);
    }

    public void removeKey(String key) {
        cache.remove(key);
        log.error("удалили ключ {}", key);
    }

    public void clear() {
        cache.clear();
        log.error("очистили Кеш");
    }


    @Scheduled(fixedDelayString = "${app.cached.scanning-interval}")
    public void removeExpiredKeys() {
        log.error("очищаем кеш от просрочников");
        for (String key : cache.keySet()) {
            CachedObject cachedObject = cache.get(key);
            if (cachedObject != null && cachedObject.isExpired())
                if (cache.remove(key, cachedObject))
                    log.error("удалили из кеша {}", key);
        }
    }

}
