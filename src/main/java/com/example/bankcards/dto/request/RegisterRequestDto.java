package com.example.bankcards.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank
        @Size(min = 3, max = 50)
        String username,
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 2, max = 50)
        String lastName,
        @NotBlank
        @Size(min = 2, max = 50)
        String firstName,
        String middleName,
        @NotNull
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in format yyyy-MM-dd")
        String birthDate,
        @NotBlank
        @Size(min = 6, max = 100)
        String password
) {}