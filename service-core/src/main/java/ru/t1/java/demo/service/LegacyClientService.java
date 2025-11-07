package ru.t1.java.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import r1.t1.monitoring.starter.annotation.LogDataSourceError;
import r1.t1.monitoring.starter.annotation.Metric;
import ru.t1.java.demo.aop.my.Cached;
import ru.t1.java.demo.dto.ClientDto;
import ru.t1.java.demo.exception.ClientException;
import ru.t1.java.demo.model.Client;
import ru.t1.java.demo.repository.ClientRepository;
import ru.t1.java.demo.util.ClientMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class LegacyClientService extends AbstractCrudService<Client, ClientDto> implements ClientService{
    public static final String ACCOUNT_ID_IS_NULL = "accountId is null";
    public static final String ACCOUNT_NOT_FOUND = "account not found";

    private final ClientRepository repository;
    private final Map<Long, Client> cache;



    public LegacyClientService(ClientRepository repository) {
        this.repository = repository;
        this.cache = new HashMap<>();
    }


    @Override
    protected CrudRepository<Client, Long> getRepository() {
        return repository;
    }

    @Override
    protected Function<Client, ClientDto> toDto() {
        return ClientMapper::toDto;
    }

    @Override
    protected Function<ClientDto, Client> toEntity() {
        return ClientMapper::toEntity;
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
    public Page<ClientDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(ClientMapper::toDto);
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
    public Optional<ClientDto> findById(Long id) {
        return super.findById(id);
    }

    @Override
    @LogDataSourceError
    @Metric
    @Cached
    public List<ClientDto> findAllByIds(List<Long> ids) {
        return super.findAllByIds(ids);
    }

    @Override
    @LogDataSourceError
    @Metric
    public ClientDto save(ClientDto dto) {
        return super.save(dto);
    }

    @Override
    @LogDataSourceError
    @Metric
    public Iterable<ClientDto> saveAll(Collection<ClientDto> dtos) {
        return super.saveAll(dtos);
    }

    @Override
    @LogDataSourceError
    @Metric
    public void delete(ClientDto dto) {
        super.delete(dto);
    }

    @Override
    @LogDataSourceError
    @Metric
    public void deleteAllByIds(List<Long> ids) {
        super.deleteAllByIds(ids);
    }

    @Override
    @LogDataSourceError
    @Metric
    public ClientDto findByClientId(UUID clientId) {
        if (clientId == null)
            throw new IllegalArgumentException(ACCOUNT_ID_IS_NULL);
        Optional<Client> client = repository.findByClientId(clientId);
        if (client.isPresent()) {
            return ClientMapper.toDto(client.get());
        } else {
            throw new ClientException(ACCOUNT_NOT_FOUND);
        }
    }
}
