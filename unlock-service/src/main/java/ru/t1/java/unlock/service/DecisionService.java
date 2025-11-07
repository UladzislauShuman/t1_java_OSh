package ru.t1.java.unlock.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class DecisionService {
    public boolean shouldUnlock() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
