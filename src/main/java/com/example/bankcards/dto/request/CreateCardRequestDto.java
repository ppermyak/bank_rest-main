package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateCardRequestDto(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String cardNumber,
        @NotBlank(message = "Expiry date is required")
        String expiryDate
) {}