package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {
    CardResponseDto toResponseDto(Card card);
}