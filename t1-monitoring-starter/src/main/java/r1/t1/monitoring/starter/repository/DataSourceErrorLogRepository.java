package r1.t1.monitoring.starter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import r1.t1.monitoring.starter.model.DataSourceErrorLog;

@Repository
public interface DataSourceErrorLogRepository extends CrudRepository<DataSourceErrorLog, Long> {
    Page<DataSourceErrorLog> findAll(Pageable pageable);
}
