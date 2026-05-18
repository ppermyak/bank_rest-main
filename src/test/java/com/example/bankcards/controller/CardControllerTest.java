package com.example.bankcards.controller;

import com.example.bankcards.TestData;
import com.example.bankcards.dto.request.CreateCardRequestDto;
import com.example.bankcards.dto.request.UpdateCardStatusRequestDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.service.CardService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CardController Tests")
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID TEST_CARD_ID = TestData.TEST_CARD_ID;

    @Test
    @DisplayName("POST /cards - ADMIN should create card and return 201")
    @WithMockUser(roles = "ADMIN")
    void createCard_AsAdmin_ShouldReturnCreated() throws Exception {
        CreateCardRequestDto request = TestData.validCreateCardRequest();
        CardResponseDto response = TestData.validCardResponseDto();

        when(cardService.createCard(any(CreateCardRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TestData.TEST_CARD_ID.toString()))
                .andExpect(jsonPath("$.cardNumberMasked").value(TestData.TEST_CARD_MASKED));
    }

    @Test
    @DisplayName("GET /cards/all - ADMIN should return page of cards")
    @WithMockUser(roles = "ADMIN")
    void getAllCards_AsAdmin_ShouldReturnPageOfCards() throws Exception {
        Page<CardResponseDto> page = new PageImpl<>(
                List.of(TestData.validCardResponseDto()),
                PageRequest.of(0, 10),
                1
        );

        when(cardService.getAllCards(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/cards/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("DELETE /cards/{id} - ADMIN should delete card and return 204")
    @WithMockUser(roles = "ADMIN")
    void deleteCard_AsAdmin_ShouldReturnNoContent() throws Exception {
        doNothing().when(cardService).deleteCard(TEST_CARD_ID);

        mockMvc.perform(delete("/cards/{id}", TEST_CARD_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /cards/{id}/status - ADMIN should update status and return 200")
    @WithMockUser(roles = "ADMIN")
    void updateCardStatus_AsAdmin_ShouldReturnOk() throws Exception {
        UpdateCardStatusRequestDto request = new UpdateCardStatusRequestDto("BLOCKED");
        CardResponseDto response = TestData.blockedCardResponse();

        when(cardService.updateCardStatus(eq(TEST_CARD_ID), eq("BLOCKED"))).thenReturn(response);

        mockMvc.perform(patch("/cards/{id}/status", TEST_CARD_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @DisplayName("PATCH /cards/{id}/block - ADMIN should confirm block and return 200")
    @WithMockUser(roles = "ADMIN")
    void confirmBlockCard_AsAdmin_ShouldReturnOk() throws Exception {
        CardResponseDto response = TestData.blockedCardResponse();

        when(cardService.blockCard(TEST_CARD_ID)).thenReturn(response);

        mockMvc.perform(patch("/cards/{id}/block", TEST_CARD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @DisplayName("POST /cards/{id}/block-request - USER should request block and return 200")
    @WithMockUser(roles = "USER")
    void requestBlockCard_AsUser_ShouldReturnOk() throws Exception {
        CardResponseDto response = TestData.pendingBlockCardResponse();

        when(cardService.requestBlockCard(eq(TEST_CARD_ID), any(String.class))).thenReturn(response);

        mockMvc.perform(post("/cards/{id}/block-request", TEST_CARD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_BLOCK"));
    }

    @Test
    @DisplayName("GET /cards - USER should return own cards")
    @WithMockUser(roles = "USER")
    void getMyCards_AsUser_ShouldReturnOwnCards() throws Exception {
        Page<CardResponseDto> page = new PageImpl<>(
                List.of(TestData.validCardResponseDto()),
                PageRequest.of(0, 10),
                1
        );

        when(cardService.getUserCards(any(String.class), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("GET /cards/{id}/balance - USER should return balance")
    @WithMockUser(roles = "USER")
    void getCardBalance_AsUser_ShouldReturnBalance() throws Exception {
        when(cardService.getCardBalance(eq(TEST_CARD_ID), any(String.class)))
                .thenReturn(TestData.cardBalanceResponse());

        mockMvc.perform(get("/cards/{id}/balance", TEST_CARD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(TestData.TEST_CARD_BALANCE));
    }
}