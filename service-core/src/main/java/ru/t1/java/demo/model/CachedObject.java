package ru.t1.java.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CachedObject {
    private final Object value;
    private final long expiryTimeMs;

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTimeMs;
    }
}
