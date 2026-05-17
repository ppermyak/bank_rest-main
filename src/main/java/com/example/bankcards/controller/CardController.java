package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CreateCardRequestDto;
import com.example.bankcards.dto.request.UpdateCardStatusRequestDto;
import com.example.bankcards.dto.response.CardBalanceResponseDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.service.CardService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management", description = "API для управления банковскими картами")
public class CardController {

    private final CardService cardService;

    @Operation(summary = "Создание карты", description = "Только для ADMIN. Создаёт карту для указанного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Карта создана",
                    content = @Content(schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Неверные данные")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CreateCardRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(request));
    }

    @Operation(summary = "Все карты", description = "Только для ADMIN. Получение всех карт с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение списка карт",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль ADMIN)")
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponseDto>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @Operation(summary = "Изменение статуса карты", description = "Только для ADMIN. Блокировка/активация карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус обновлён",
                    content = @Content(schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "400", description = "Неверный статус")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> updateCardStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCardStatusRequestDto request) {
        return ResponseEntity.ok(cardService.updateCardStatus(id, request.status()));
    }

    @Operation(summary = "Удаление карты", description = "Только для ADMIN. Удаление карты из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Подтверждение блокировки карты", description = "Только для ADMIN. Подтверждает запрос на блокировку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта заблокирована",
                    content = @Content(schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "400", description = "Нет запроса на блокировку")
    })
    @PatchMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDto> confirmBlockCard(@PathVariable UUID id) {
        return ResponseEntity.ok(cardService.blockCard(id));
    }

    @Operation(summary = "Запрос на блокировку карты", description = "Только для USER. Отправляет запрос на блокировку карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос отправлен (статус PENDING_BLOCK)",
                    content = @Content(schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль USER)"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @PostMapping("/{id}/block-request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardResponseDto> requestBlockCard(
            @PathVariable UUID id,
            Principal principal) {
        return ResponseEntity.ok(cardService.requestBlockCard(id, principal.getName()));
    }

    @Operation(summary = "Мои карты", description = "Только для USER. Получение списка своих карт с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение списка карт",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль USER)")
    })
    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardResponseDto>> getMyCards(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getUserCards(principal.getName(), pageable));
    }

    @Operation(summary = "Баланс карты", description = "Только для USER. Получение баланса своей карты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение баланса",
                    content = @Content(schema = @Schema(implementation = CardBalanceResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (не владелец карты)"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @GetMapping("/{id}/balance")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardBalanceResponseDto> getCardBalance(@PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(cardService.getCardBalance(id, principal.getName()));
    }
}