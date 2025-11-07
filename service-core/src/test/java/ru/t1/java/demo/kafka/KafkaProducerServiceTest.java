package ru.t1.java.demo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(KafkaProducerServiceTest.TestKafkaListener.class)
@DirtiesContext
@ActiveProfiles("test")
@EmbeddedKafka (
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9094", "port=9094"},
        topics = {KafkaProducerServiceTest.TEST_TOPIC}
)
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class KafkaProducerServiceTest {
    static final String TEST_TOPIC = "test-topic";

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private TestKafkaListener testKafkaListener;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendMessageToKafkaSuccessfully() throws InterruptedException, JsonProcessingException {
        SimplePayload payload = new SimplePayload("test-id", "test-value");
        String errorType = "TEST_ERROR";

        testKafkaListener.resetLatch();

        boolean result = kafkaProducerService.sendMessage(TEST_TOPIC, payload, errorType);
        assertTrue(result);

        // жду, пока не получу
        boolean messageReceived = testKafkaListener.getLatch().await(5, TimeUnit.SECONDS);
        assertTrue(messageReceived);

        AtomicReference<ConsumerRecord<String, String>> receivedRecord = testKafkaListener.getReceiveRecord();
        assertNotNull(receivedRecord);

        //тело
        SimplePayload receivedPayload = objectMapper.readValue(receivedRecord.get().value(), SimplePayload.class);
        assertEquals(payload.getId(), receivedPayload.getId());
        assertEquals(payload.getValue(), receivedPayload.getValue());

        //заголовок
        byte[] headerVulue = receivedRecord.get().headers().lastHeader(KafkaProducerService.HEADER_ERROR_TYPE).value();
        assertNotNull(headerVulue);
        assertEquals(errorType, new String(headerVulue, StandardCharsets.UTF_8));

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class SimplePayload {
        private String id;
        private String value;
    }

    @Component
    @Data
    public static class TestKafkaListener {
        private CountDownLatch latch = new CountDownLatch(1);
        private final AtomicReference<ConsumerRecord<String, String>> receiveRecord = new AtomicReference<>();

        @KafkaListener(topics =  TEST_TOPIC, groupId = "test-group")
        public void listen(ConsumerRecord<String, String> record) {
            receiveRecord.set(record);
            latch.countDown();
        }

        public void resetLatch() {
            latch = new CountDownLatch(1);
            receiveRecord.set(null);
        }
    }
}