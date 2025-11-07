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

import r1.t1.monitoring.starter.annotation.Metric;
import ru.t1.java.demo.dto.TransactionDto;
import ru.t1.java.demo.service.TransactionService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    private final ObjectMapper objectMapper;

    @GetMapping
    @Metric
    public PagedModel<TransactionDto> getAll(Pageable pageable) {
        Page<TransactionDto> transactionDtos = transactionService.findAll(pageable);
        return new PagedModel<>(transactionDtos);
    }

    @GetMapping("/{id}")
    @Metric
    public TransactionDto getOne(@PathVariable Long id) {
        Optional<TransactionDto> transactionOptional = transactionService.findById(id);
        return transactionOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @GetMapping("/by-ids")
    @Metric
    public Iterable<TransactionDto> getMany(@RequestParam List<Long> ids) {
        return transactionService.findAllByIds(ids);
    }

    @PostMapping
    @Metric
    public TransactionDto create(@RequestBody TransactionDto transactionDto) {
        return transactionService.save(transactionDto);
    }

    @PatchMapping("/{id}")
    @Metric
    public TransactionDto patch(@PathVariable Long id, @RequestBody JsonNode patchNode) throws IOException {
        TransactionDto transactionDto = transactionService.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        objectMapper.readerForUpdating(transactionDto).readValue(patchNode);

        return transactionService.save(transactionDto);
    }

    @PatchMapping
    @Metric
    public List<Long> patchMany(@RequestParam List<Long> ids, @RequestBody JsonNode patchNode) throws IOException {
        Collection<TransactionDto> transactionDtos = (Collection) transactionService.findAllByIds(ids);

        for (TransactionDto transaction : transactionDtos) {
            objectMapper.readerForUpdating(transaction).readValue(patchNode);
        }

        Iterable<TransactionDto> resultTransactions = transactionService.saveAll(transactionDtos);
        return StreamSupport.stream(resultTransactions.spliterator(), false)
                .map(TransactionDto::getId)
                .toList();
    }

    @DeleteMapping("/{id}")
    @Metric
    public TransactionDto delete(@PathVariable Long id) {
        TransactionDto transactionDto = transactionService.findById(id).orElse(null);
        if (transactionDto != null) {
            transactionService.delete(transactionDto);
        }
        return transactionDto;
    }

    @DeleteMapping
    @Metric
    public void deleteMany(@RequestParam List<Long> ids) {
        transactionService.deleteAllByIds(ids);
    }
}
