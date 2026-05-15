package com.example.bankcards.dto.response;

import java.math.BigDecimal;

public record CardBalanceResponseDto(
        BigDecimal balance
) {}