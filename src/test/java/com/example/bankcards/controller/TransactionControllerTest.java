package com.example.bankcards.controller;

import com.example.bankcards.TestData;
import com.example.bankcards.dto.request.CreateTransactionRequestDto;
import com.example.bankcards.dto.response.TransactionResponseDto;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TransactionController Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID TEST_FROM_CARD_ID = TestData.TEST_CARD_ID;
    private static final UUID TEST_TO_CARD_ID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");

    @Test
    @DisplayName("POST /transactions - USER should create transaction and return 201")
    @WithMockUser(roles = "USER")
    void createTransaction_AsUser_ShouldReturnCreated() throws Exception {
        CreateTransactionRequestDto request = TestData.validCreateTransactionRequest(TEST_FROM_CARD_ID, TEST_TO_CARD_ID);
        TransactionResponseDto response = TestData.validTransactionResponseDto(TEST_FROM_CARD_ID, TEST_TO_CARD_ID);

        when(transactionService.createTransaction(any(CreateTransactionRequestDto.class), any(String.class)))
                .thenReturn(response);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TestData.TEST_TRANSACTION_ID.toString()))
                .andExpect(jsonPath("$.amount").value(TestData.TEST_TRANSACTION_AMOUNT));
    }

    @Test
    @DisplayName("GET /transactions - USER should return page of transactions")
    @WithMockUser(roles = "USER")
    void getMyTransactions_AsUser_ShouldReturnPageOfTransactions() throws Exception {
        Page<TransactionResponseDto> page = new PageImpl<>(
                List.of(TestData.validTransactionResponseDto(TEST_FROM_CARD_ID, TEST_TO_CARD_ID)),
                PageRequest.of(0, 10),
                1
        );

        when(transactionService.getUserTransactions(any(String.class), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/transactions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}