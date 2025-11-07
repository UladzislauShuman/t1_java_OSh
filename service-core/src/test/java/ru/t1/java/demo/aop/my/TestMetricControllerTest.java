package ru.t1.java.demo.aop.my;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import r1.t1.monitoring.starter.kafka.MonitoringKafkaProducerService;
import r1.t1.monitoring.starter.repository.MetricLogRepository;
import ru.t1.java.demo.T1JavaDemoApplication;
import ru.t1.java.demo.kafka.KafkaProducerService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = T1JavaDemoApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "t1.monitoring.metric-properties.execute-time-limit-ms=100"
})
class TestMetricControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MetricLogRepository metricLogRepository;
    @MockBean
    private MonitoringKafkaProducerService monitoringKafkaProducerService;
    @MockBean
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        metricLogRepository.deleteAll();
    }

    @Test
    void notCreateLog_forGoodMetricEndpoint() throws Exception {
        mockMvc.perform(get("/test/metric/metric_good"))
                .andExpect(status().isOk());

        assertEquals(0, metricLogRepository.count());
    }

    @Test
    void createLog_forBadMetricEndpointAndKafkaFails() throws Exception {

        when(monitoringKafkaProducerService.sendMessage(anyString(), any(), anyString()))
                .thenReturn(false);

        mockMvc.perform(get("/test/metric/metric_bad"))
                .andExpect(status().isOk());

        assertEquals(1, metricLogRepository.count());
    }
}