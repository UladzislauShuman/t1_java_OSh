package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.aop.my.Metric;
import ru.t1.java.demo.dto.MetricLogDto;
import ru.t1.java.demo.service.MetricLogService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/test/metric")
@RequiredArgsConstructor
public class TestMetricController {

    public static final int BAD_LIMIT_TIME = 250;
    public static final int GOOD_LIMIT_TIME = 1;

    private final MetricLogService service;

    @Metric
    @GetMapping("/metric_bad")
    public void testMetric_badTimeLimits() throws InterruptedException {
        Thread.sleep(BAD_LIMIT_TIME);
    }

    @Metric
    @GetMapping("/metric_good")
    public void testMetric_goodLimits() throws InterruptedException {
        Thread.sleep(GOOD_LIMIT_TIME);
    }

    @Metric
    @GetMapping("metrics/all")
    public List<MetricLogDto> getAll() {
        return service.findAll();
    }


}
