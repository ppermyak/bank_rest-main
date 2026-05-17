package com.example.bankcards.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String username,
        String email,
        String lastName,
        String firstName,
        String middleName,
        LocalDate birthDate,
        String role,
        LocalDateTime createdAt
) {}