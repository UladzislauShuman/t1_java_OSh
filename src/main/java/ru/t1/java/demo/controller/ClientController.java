package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.aop.HandlingResult;
import ru.t1.java.demo.aop.Track;
import ru.t1.java.demo.aop.LogException;
import ru.t1.java.demo.aop.my.Metric;
import ru.t1.java.demo.exception.ClientException;
import ru.t1.java.demo.service.impl.ClientServiceImpl;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientServiceImpl clientService;

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
