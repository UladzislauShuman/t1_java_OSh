package ru.t1.java.demo.service;

import ru.t1.java.demo.dto.ClientDto;

import java.util.UUID;

public interface ClientService extends CrudService<ClientDto> {
    ClientDto findByClientId(UUID clientId);
}

