package com.example.bankcards.mapper;

import com.example.bankcards.dto.request.RegisterRequestDto;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(RegisterRequestDto request);
}