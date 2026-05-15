package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateCardStatusRequestDto(
        @NotBlank
        @Pattern(regexp = "ACTIVE|BLOCKED|EXPIRED",
                message = "Status must be ACTIVE, BLOCKED, or EXPIRED")
        String status
) {
}