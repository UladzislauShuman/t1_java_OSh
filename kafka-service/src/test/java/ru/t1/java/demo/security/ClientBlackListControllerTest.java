package ru.t1.java.demo.security;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import static org.mockito.BDDMockito.given;


import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@Slf4j
@WebMvcTest(ClientBlackListController.class)
@Import(SecurityConfig.class)
class ClientBlackListControllerTest {
    public static final String USERNAME = "service-user";
    public static final String PASSWORD = "service-password";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BlackListService blackListService;
    @MockBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        UserDetails testUser = User.builder()
                .username(USERNAME)
                .password("{noop}" + PASSWORD)
                .roles("SERVICE")
                .build();
        given(userDetailsService.loadUserByUsername("service-user")).willReturn(testUser);
    }

    @Test
    void returnTrueWhenClientIsInBlacklist() throws Exception {
        UUID clientId = UUID.randomUUID();

        when(blackListService.isClientBlackListed(any(UUID.class))).thenReturn(true);

        mockMvc.perform(get("/api/clients/{clientId}/check-blacklist", clientId)
                .with(httpBasic("service-user", "service-password")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clientId", is(clientId.toString())))
                .andExpect(jsonPath("$.blackListed", is(true)));
    }
    @Test
    void returnFalseWhenClientIsNotInBlacklist() throws Exception {
        UUID clientId = UUID.randomUUID();

        when(blackListService.isClientBlackListed(any(UUID.class))).thenReturn(false);

        mockMvc.perform(get("/api/clients/{clientId}/check-blacklist", clientId)
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clientId", is(clientId.toString())))
                .andExpect(jsonPath("$.blackListed", is(false)));
    }

    @Test
    void returnUnauthorizedWithoutCredentials() throws Exception {
        UUID clientId = UUID.randomUUID();
        when(blackListService.isClientBlackListed(any(UUID.class))).thenReturn(false);

        mockMvc.perform(get("/api/clients/{clientId}/check-blacklist", clientId))
                .andExpect(status().isUnauthorized()); // 401
    }
}