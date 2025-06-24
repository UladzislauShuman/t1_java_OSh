package ru.t1.java.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.t1.java.demo.model.DataSourceErrorLog;

@Repository
public interface DataSourceErrorLogRepository extends CrudRepository<DataSourceErrorLog, Long> {
    Page<DataSourceErrorLog> findAll(Pageable pageable);
}
