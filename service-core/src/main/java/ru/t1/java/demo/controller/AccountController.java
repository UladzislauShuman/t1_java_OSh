package ru.t1.java.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ru.t1.java.demo.aop.my.Metric;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.service.AccountService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final ObjectMapper objectMapper;

    @Metric
    @GetMapping
    public PagedModel<AccountDto> getAll(Pageable pageable) {
        Page<AccountDto> accountDtos = accountService.findAll(pageable);
        return new PagedModel<>(accountDtos);
    }

    @GetMapping("/{id}")
    @Metric
    public AccountDto getOne(@PathVariable Long id) {
        Optional<AccountDto> accountOptional = accountService.findById(id);
        return accountOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @GetMapping("/by-ids")
    @Metric
    public Iterable<AccountDto> getMany(@RequestParam List<Long> ids) {
        return accountService.findAllByIds(ids);
    }

    @GetMapping("/by-account-id")
    @Metric
    public AccountDto getByAccountId(@RequestParam UUID accountId) {
        return accountService.findByAccountId(accountId);
    }

    @PostMapping
    @Metric
    public AccountDto create(@RequestBody AccountDto accountDto) {
        return accountService.save(accountDto);
    }

    @PatchMapping("/{id}")
    @Metric
    public AccountDto patch(@PathVariable Long id, @RequestBody JsonNode patchNode) throws IOException {
        AccountDto accountDto = accountService.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        objectMapper.readerForUpdating(accountDto).readValue(patchNode);

        return accountService.save(accountDto);
    }

    @PatchMapping
    @Metric
    public List<Long> patchMany(@RequestParam List<Long> ids, @RequestBody JsonNode patchNode) throws IOException {
        Collection<AccountDto> accountDtos = accountService.findAllByIds(ids);

        for (AccountDto account : accountDtos) {
            objectMapper.readerForUpdating(account).readValue(patchNode);
        }

        Iterable<AccountDto> resultAccounts = accountService.saveAll(accountDtos);
        return StreamSupport.stream(resultAccounts.spliterator(), false)
                .map(AccountDto::getId)
                .toList();
    }

    @DeleteMapping("/{id}")
    @Metric
    public AccountDto delete(@PathVariable Long id) {
        AccountDto accountDto = accountService.findById(id).orElse(null);
        if (accountDto != null) {
            accountService.delete(accountDto);
        }
        return accountDto;
    }

    @DeleteMapping
    @Metric
    public void deleteMany(@RequestParam List<Long> ids) {
        accountService.deleteAllByIds(ids);
    }
}
