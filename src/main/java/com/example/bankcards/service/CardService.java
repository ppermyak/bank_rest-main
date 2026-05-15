package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateCardRequestDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final CardMapper cardMapper;

    @Transactional
    public CardResponseDto createCard(CreateCardRequestDto dto) {
        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new RuntimeException("User not found: " + dto.username()));

        String encryptedCardNumber = encryptionUtil.encrypt(dto.cardNumber());
        String maskedNumber = "**** **** **** " + dto.cardNumber().substring(12);

        YearMonth expiryYearMonth = YearMonth.parse(dto.expiryDate(), DateTimeFormatter.ofPattern("MM/yy"));
        LocalDate expiryDate = expiryYearMonth.atDay(1);

        Card card = new Card();
        card.setCardNumber(encryptedCardNumber);
        card.setCardNumberMasked(maskedNumber);
        card.setOwnerName(user.getOwnerName());
        card.setExpiryDate(expiryDate);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(java.math.BigDecimal.ZERO);
        card.setUser(user);

        Card savedCard = cardRepository.save(card);
        return cardMapper.toResponseDto(savedCard);
    }

    public Page<CardResponseDto> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(cardMapper::toResponseDto);
    }

    @Transactional
    public CardResponseDto updateCardStatus(UUID cardId, String status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        CardStatus newStatus = CardStatus.valueOf(status.toUpperCase());
        card.setStatus(newStatus);
        card.setUpdatedAt(LocalDateTime.now());

        Card updatedCard = cardRepository.save(card);
        return cardMapper.toResponseDto(updatedCard);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        cardRepository.delete(card);
    }
}