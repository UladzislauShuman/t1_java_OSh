package r1.t1.monitoring.starter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;
import r1.t1.monitoring.starter.aspect.LogDataSourceErrorAspect;
import r1.t1.monitoring.starter.aspect.MetricAspect;
import r1.t1.monitoring.starter.kafka.MonitoringKafkaProducerService;
import r1.t1.monitoring.starter.repository.DataSourceErrorLogRepository;
import r1.t1.monitoring.starter.repository.MetricLogRepository;

@Configuration
@EnableConfigurationProperties(MonitoringProperties.class)
@ConditionalOnProperty(prefix = "t1.monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({JoinPoint.class, KafkaTemplate.class})
@EnableJpaRepositories(basePackages = "r1.t1.monitoring.starter.repository")
@EntityScan(basePackages = "r1.t1.monitoring.starter.model")
@RequiredArgsConstructor
public class MonitoringAutoConfiguration {
    private final MonitoringProperties properties;

    @Bean
    @Qualifier("monitoringObjectMapper")
    public ObjectMapper monitoringObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public MonitoringKafkaProducerService monitoringKafkaProducerService(
            KafkaTemplate<String, String> kafkaTemplate,
            @Qualifier("monitoringObjectMapper") ObjectMapper mapper) {
        return new MonitoringKafkaProducerService(kafkaTemplate, mapper);
    }

    @Bean
    public MetricAspect metricAspect(MetricLogRepository repository, MonitoringKafkaProducerService service) {
        return new MetricAspect(
                repository,
                service,
                properties.getMetricProperties().getExecuteTimeLimitMs(),
                properties.getMetricProperties().getKafkaTopic());
    }

    @Bean
    public LogDataSourceErrorAspect logDataSourceErrorAspect(
            DataSourceErrorLogRepository repository, MonitoringKafkaProducerService service) {
        return new LogDataSourceErrorAspect(
                repository,
                service,
                properties.getDataSourceErrorLogProperties().getKafkaTopic());
    }
}
