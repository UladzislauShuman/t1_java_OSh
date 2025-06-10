package r1.t1.monitoring.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "t1.monitoring")
public class MonitoringProperties {

    private boolean enabled = true;
    private MetricProperties metricProperties = new MetricProperties();
    private DataSourceErrorLogProperties dataSourceErrorLogProperties = new DataSourceErrorLogProperties();

    @Data
    public static class MetricProperties {
        private long executeTimeLimitMs = 100;
        private String kafkaTopic = "t1_demo_metrics";
    }

    @Data
    public static class DataSourceErrorLogProperties {
        private String kafkaTopic = "t1_demo_metrics";
    }
}
