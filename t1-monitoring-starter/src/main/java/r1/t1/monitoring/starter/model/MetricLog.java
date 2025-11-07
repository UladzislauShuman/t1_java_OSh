package r1.t1.monitoring.starter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "time_limit_exceed_log")
public class MetricLog extends AbstractPersistable<Long> implements HasId {
    // id

    @Column(name = "method_signature")
    private String methodSignature;

    @Column(name = "execution_time_ms")
    private long executionTimeMs;

    @Column(name = "limit_ms")
    private long limitMs;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

}
