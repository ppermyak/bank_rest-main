package com.example.bankcards.dto.response;

public record AuthResponseDto(
        String token,
        String username,
        String role
) {
}