package ru.t1.java.demo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BlackListService {

    @Value("${app.fraud.test-blacklist}")
    private boolean isTesting;

    public boolean isClientBlackListed(UUID clientId) {
        return isTesting; //|| ThreadLocalRandom.current().nextInt(10) == 0;
    }

}
