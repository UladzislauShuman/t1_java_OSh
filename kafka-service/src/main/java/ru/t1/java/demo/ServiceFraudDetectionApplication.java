package ru.t1.java.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ServiceFraudDetectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceFraudDetectionApplication.class, args);
    }
}