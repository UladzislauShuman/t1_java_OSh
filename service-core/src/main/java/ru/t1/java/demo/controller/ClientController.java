package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import r1.t1.monitoring.starter.annotation.Metric;
import ru.t1.java.demo.aop.old.HandlingResult;
import ru.t1.java.demo.aop.old.Track;
import ru.t1.java.demo.aop.old.LogException;
import ru.t1.java.demo.exception.ClientException;
import ru.t1.java.demo.service.ClientService;
import ru.t1.java.demo.service.impl.ClientServiceImpl;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @LogException
    @Track
    @GetMapping(value = "/client")
    @HandlingResult
    @Metric
    public void doSomething() throws IOException, InterruptedException {
//        try {
//            clientService.parseJson();
        Thread.sleep(3000L);
        throw new ClientException();
//        } catch (Exception e) {
//            log.info("Catching exception from ClientController");
//            throw new ClientException();
//        }
    }

}
