package ru.t1.java.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractCrudService<E extends Persistable<Long>, D> {

    protected abstract CrudRepository<E, Long> getRepository();
    protected abstract Function<E, D> toDto();
    protected abstract Function<D, E> toEntity();

    public Page<D> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("not supported");
    }

    public Optional<D> findById(Long id) {
        if (id == null) throw new IllegalArgumentException("id == null");
        if (id < 1) throw new IllegalArgumentException("id less than 1");
        // if (id == 1) throw new RuntimeException("test");
        E entity = getRepository().findById(id).orElse(null);
        return Optional.ofNullable(entity).map(toDto());
    }

    public List<D> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        Iterable<E> entities = getRepository().findAllById(ids);
        return StreamSupport.stream(entities.spliterator(), false)
                .map(toDto())
                .collect(Collectors.toList());
    }

    public D save(D dto) {
        if (dto == null) throw new IllegalArgumentException("dto == null");
        E entity = toEntity().apply(dto);
        getRepository().save(entity);
        return dto;
    }

    public Iterable<D> saveAll(Collection<D> dtos) {
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        if (dtos.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("dtos contains null");
        }

        List<E> entities = dtos.stream().map(toEntity()).collect(Collectors.toList());
        List<E> saved = (List<E>) getRepository().saveAll(entities);
        return saved.stream().map(toDto()).collect(Collectors.toList());
    }

    public void delete(D dto) {
        if (dto == null) throw new IllegalArgumentException("dto is null");
        Optional<E> entity = getRepository().findById(toEntity().apply(dto).getId());
        entity.ifPresent(getRepository()::delete);
    }

    public void deleteAllByIds(List<Long> ids) {
        if (ids == null) throw new IllegalArgumentException("ids is null");
        if (ids.isEmpty()) return;
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("ids contains null");
        }
        getRepository().deleteAllById(ids);
    }
}
