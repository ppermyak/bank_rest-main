package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CreateCardRequestDto;
import com.example.bankcards.dto.request.UpdateCardStatusRequestDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CardService cardService;

    @PostMapping("/cards")
    public ResponseEntity<CardResponseDto> createCard(
            @Valid @RequestBody CreateCardRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(request));
    }

    @GetMapping("/cards")
    public ResponseEntity<Page<CardResponseDto>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @PatchMapping("/cards/{id}/status")
    public ResponseEntity<CardResponseDto> updateCardStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCardStatusRequestDto request) {

        return ResponseEntity.ok(cardService.updateCardStatus(id, request.status()));
    }
}