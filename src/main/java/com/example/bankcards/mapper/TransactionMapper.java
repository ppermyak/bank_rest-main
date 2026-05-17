package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.TransactionResponseDto;
import com.example.bankcards.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "fromCardId", source = "fromCard.id")
    @Mapping(target = "toCardId", source = "toCard.id")
    @Mapping(target = "fromCardMasked", source = "fromCard.cardNumberMasked")
    @Mapping(target = "toCardMasked", source = "toCard.cardNumberMasked")
    @Mapping(target = "status", source = "status")
    TransactionResponseDto toResponseDto(Transaction transaction);
}