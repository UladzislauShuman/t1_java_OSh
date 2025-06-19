package ru.t1.java.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.t1.java.demo.config.JacksonConfig;
import ru.t1.java.demo.dto.AccountDto;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.security.SecurityConfig;
import ru.t1.java.demo.service.AccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, JacksonConfig.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    void whenGetAccountById_withValidId_thenReturnsAccountDto() throws Exception {
        long accountId = 1L;
        UUID accountUUID = UUID.randomUUID();

        AccountDto mockAccountDto = AccountDto.builder()
                .id(accountId)
                .accountId(accountUUID)
                .balance(new BigDecimal("123.00"))
                .status(Account.Status.OPEN)
                .build();

        given(accountService.findById(accountId)).willReturn(Optional.of(mockAccountDto));

        mockMvc.perform(get("/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.account_id", is(accountUUID.toString())))
                .andExpect(jsonPath("$.balance", is(123.00)));
    }

    @Test
    void whenGetAccountById_withInvalidId_thenReturnsNotFound() throws Exception{
        long invalidId = 1L;
        given(accountService.findById(invalidId)).willReturn(Optional.empty());

        mockMvc.perform(get("/accounts/{id}", invalidId))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenCreateAccount_withValidData_thenReturnsCreatedAccountDto() throws Exception {
        UUID accountUuid = UUID.randomUUID();
        AccountDto accountDto = AccountDto.builder()
                .id(1L)
                .accountId(accountUuid)
                .balance(BigDecimal.ZERO)
                .status(Account.Status.OPEN)
                .build();

        Account account = Account.builder()
                .accountId(accountUuid)
                .balance(BigDecimal.ZERO)
                .status(Account.Status.OPEN)
                .build();

        given(accountService.save(any(AccountDto.class))).willReturn(accountDto);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.account_id", is(accountUuid.toString())));
    }

    @Test
    void whenGetAll_thenReturnsPagedModel() throws Exception {
        AccountDto mockAccount = AccountDto.builder().id(1L).balance(BigDecimal.TEN).build();
        PageImpl<AccountDto> page = new PageImpl<>(List.of(mockAccount));

        given(accountService.findAll(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/accounts")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1L), Long.class))
                .andExpect(jsonPath("$.page.size", is(1)))
                .andExpect(jsonPath("$.page.totalElements", is(1)));
    }
}