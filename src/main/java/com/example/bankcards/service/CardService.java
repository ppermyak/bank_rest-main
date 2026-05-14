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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final CardMapper cardMapper;

    @Transactional
    public CardResponseDto createCard(CreateCardRequestDto request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.username()));

        String encryptedCardNumber = encryptionUtil.encrypt(request.cardNumber());
        String maskedNumber = "**** **** **** " + request.cardNumber().substring(12);

        YearMonth expiryYearMonth = YearMonth.parse(request.expiryDate(), DateTimeFormatter.ofPattern("MM/yy"));
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
}