package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CreateTransactionRequestDto;
import com.example.bankcards.dto.response.TransactionResponseDto;
import com.example.bankcards.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "API для управления переводами")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Создание перевода", description = "Только для USER. Перевод между своими картами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Перевод выполнен",
                    content = @Content(schema = @Schema(implementation = TransactionResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (не владелец карты)"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств или неверные данные")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody CreateTransactionRequestDto request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request, principal.getName()));
    }

    @Operation(summary = "История переводов", description = "Только для USER. Получение истории переводов с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение истории",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль USER)")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<TransactionResponseDto>> getMyTransactions(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transactionService.getUserTransactions(principal.getName(), pageable));
    }
}