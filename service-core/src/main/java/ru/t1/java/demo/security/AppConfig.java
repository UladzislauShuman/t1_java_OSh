package ru.t1.java.demo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Value("${app.services.fraud-detection.username}")
    private String username;

    @Value("${app.services.fraud-detection.password}")
    private String password;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return  restTemplateBuilder
                .basicAuthentication(username, password)
                .build();
    }
}
