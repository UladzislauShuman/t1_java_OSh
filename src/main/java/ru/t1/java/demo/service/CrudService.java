package ru.t1.java.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CrudService<DTO> {
    Page<DTO> findAll(Pageable pageable);
    Optional<DTO> findById(Long id);
    List<DTO> findAllByIds(List<Long> ids);
    DTO save(DTO dto);
    Iterable<DTO> saveAll(Collection<DTO> dtos);
    void delete(DTO dto);
    void deleteAllByIds(List<Long> ids);
}
