package com.example.bankcards.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CardResponseDto(
        UUID id,
        String cardNumberMasked,
        String ownerName,
        LocalDate expiryDate,
        String status,
        BigDecimal balance
) {}