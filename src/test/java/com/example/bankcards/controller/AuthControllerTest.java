package com.example.bankcards.controller;

import com.example.bankcards.TestData;
import com.example.bankcards.dto.request.LoginRequestDto;
import com.example.bankcards.dto.response.AuthResponseDto;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /auth/login - Should return 200 with token")
    void login_ShouldReturnToken() throws Exception {
        LoginRequestDto request = TestData.validLoginRequest();
        AuthResponseDto response = new AuthResponseDto(
                TestData.TEST_JWT_TOKEN,
                TestData.TEST_USERNAME,
                TestData.ROLE_USER
        );

        when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TestData.TEST_JWT_TOKEN))
                .andExpect(jsonPath("$.username").value(TestData.TEST_USERNAME))
                .andExpect(jsonPath("$.role").value(TestData.ROLE_USER));
    }

    @Test
    @DisplayName("POST /auth/register - Should return 201 when user created")
    void register_ShouldReturnCreated() throws Exception {
        var request = TestData.validRegisterRequest();
        AuthResponseDto response = new AuthResponseDto(
                TestData.TEST_JWT_TOKEN,
                TestData.TEST_USERNAME,
                TestData.ROLE_USER
        );

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(TestData.TEST_JWT_TOKEN))
                .andExpect(jsonPath("$.username").value(TestData.TEST_USERNAME));
    }
}