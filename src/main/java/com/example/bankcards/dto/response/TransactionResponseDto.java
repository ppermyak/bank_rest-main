package com.example.bankcards.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDto(
        UUID id,
        UUID fromCardId,
        UUID toCardId,
        String fromCardMasked,
        String toCardMasked,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt
) {}