package r1.t1.monitoring.starter.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitoringKafkaProducerService {

    public static final String HEADER_ERROR_TYPE = "error_type";
    private final KafkaTemplate<String , String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public boolean sendMessage(String topic, Object payload, String errorType) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);// переводим в JSON
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, jsonPayload); // то, что будет далее отправлено
            record.headers().add(HEADER_ERROR_TYPE, errorType.getBytes(StandardCharsets.UTF_8));

            log.error("attempting is not torture: sending of message to Kafka. topic: {}, header: {}: {}, payload: {}",
                    topic, HEADER_ERROR_TYPE, errorType, jsonPayload);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(record); // асинхронно
            setWhenComplete(future, jsonPayload, topic);
            return true;
        } catch (JsonProcessingException e) {
            log.error("problems with serializing of payload to json: {}", payload, e);
            return false;
        } catch (Exception e) {
            log.error("something bad is going with sending of message for Kafka: topic: {}, payload: {}",
                    topic, payload);
            return false;
        }
    }

    private void setWhenComplete(CompletableFuture<SendResult<String, String>> future, String jsonPayload, String topic) {
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.error("everything is fine: message=[{}] with offset=[{}] is running to topic=[{}]",
                        jsonPayload,result.getRecordMetadata().offset(), topic);
            } else {
                log.error("everything is bad: message=[{}] is not running to topic=[{}], because: {}",
                        jsonPayload, topic, exception.getMessage(), exception);
            }
        });
    }

}
