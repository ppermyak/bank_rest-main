package com.example.bankcards;

import com.example.bankcards.dto.request.CreateCardRequestDto;
import com.example.bankcards.dto.request.CreateTransactionRequestDto;
import com.example.bankcards.dto.request.LoginRequestDto;
import com.example.bankcards.dto.request.RegisterRequestDto;
import com.example.bankcards.dto.response.CardBalanceResponseDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.dto.response.TransactionResponseDto;
import com.example.bankcards.dto.response.UserResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TestData {

    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_PASSWORD = "password123";
    public static final String TEST_ENCODED_PASSWORD = "$2a$10$encoded";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_JWT_TOKEN = "eyJhbGciOiJIUzUxMiJ9.test-token";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final UUID TEST_CARD_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final String TEST_CARD_NUMBER = "1234567890123456";
    public static final String TEST_ENCRYPTED_CARD_NUMBER = "encrypted_1234567890123456";
    public static final String TEST_CARD_MASKED = "**** **** **** 3456";
    public static final String TEST_CARD_OWNER = "Иванов Иван Иванович";
    public static final String TEST_CARD_EXPIRY_DATE = "12/28";
    public static final LocalDate TEST_CARD_EXPIRY_DATE_PARSED = LocalDate.of(2028, 12, 1);
    public static final BigDecimal TEST_CARD_BALANCE = BigDecimal.valueOf(1000);

    public static final UUID TEST_TRANSACTION_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    public static final BigDecimal TEST_TRANSACTION_AMOUNT = BigDecimal.valueOf(100);
    public static final String TEST_TRANSACTION_STATUS = "COMPLETED";

    public static LoginRequestDto validLoginRequest() {
        return new LoginRequestDto(TEST_USERNAME, TEST_PASSWORD);
    }

    public static RegisterRequestDto validRegisterRequest() {
        return new RegisterRequestDto(
                TEST_USERNAME,
                TEST_EMAIL,
                "Иванов",
                "Иван",
                "Иванович",
                "1990-01-01",
                TEST_PASSWORD
        );
    }

    public static CreateCardRequestDto validCreateCardRequest() {
        return new CreateCardRequestDto(
                TEST_USERNAME,
                TEST_CARD_NUMBER,
                TEST_CARD_EXPIRY_DATE
        );
    }

    public static CardResponseDto validCardResponseDto() {
        return new CardResponseDto(
                TEST_CARD_ID,
                TEST_CARD_MASKED,
                TEST_CARD_OWNER,
                TEST_CARD_EXPIRY_DATE_PARSED,
                CardStatus.ACTIVE.name(),
                BigDecimal.ZERO
        );
    }

    public static User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_ENCODED_PASSWORD);
        user.setRole(Role.USER);
        user.setLastName("Иванов");
        user.setFirstName("Иван");
        user.setMiddleName("Иванович");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        return user;
    }

    public static User createTestAdmin() {
        User admin = createTestUser();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);
        return admin;
    }

    public static Card createTestCard() {
        Card card = new Card();
        card.setId(TEST_CARD_ID);
        card.setCardNumber(TEST_ENCRYPTED_CARD_NUMBER);
        card.setCardNumberMasked(TEST_CARD_MASKED);
        card.setOwnerName(TEST_CARD_OWNER);
        card.setExpiryDate(TEST_CARD_EXPIRY_DATE_PARSED);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(TEST_CARD_BALANCE);
        card.setUser(createTestUser());
        return card;
    }

    public static Card createTestCardWithStatus(CardStatus status) {
        Card card = createTestCard();
        card.setStatus(status);
        return card;
    }

    public static CardResponseDto cardResponseWithStatus(CardStatus status) {
        return new CardResponseDto(
                TEST_CARD_ID,
                TEST_CARD_MASKED,
                TEST_CARD_OWNER,
                TEST_CARD_EXPIRY_DATE_PARSED,
                status.name(),
                TEST_CARD_BALANCE
        );
    }

    public static CardResponseDto activeCardResponse() {
        return cardResponseWithStatus(CardStatus.ACTIVE);
    }

    public static CardResponseDto blockedCardResponse() {
        return cardResponseWithStatus(CardStatus.BLOCKED);
    }

    public static CardResponseDto pendingBlockCardResponse() {
        return cardResponseWithStatus(CardStatus.PENDING_BLOCK);
    }

    public static CreateTransactionRequestDto validCreateTransactionRequest(UUID fromCardId, UUID toCardId) {
        return new CreateTransactionRequestDto(
                fromCardId,
                toCardId,
                TEST_TRANSACTION_AMOUNT
        );
    }

    public static TransactionResponseDto validTransactionResponseDto(UUID fromCardId, UUID toCardId) {
        return new TransactionResponseDto(
                TEST_TRANSACTION_ID,
                fromCardId,
                toCardId,
                TEST_CARD_MASKED,
                TEST_CARD_MASKED,
                TEST_TRANSACTION_AMOUNT,
                TEST_TRANSACTION_STATUS,
                LocalDateTime.now()
        );
    }

    public static CardBalanceResponseDto cardBalanceResponse() {
        return new CardBalanceResponseDto(TEST_CARD_BALANCE);
    }

    public static UserResponseDto validUserResponseDto() {
        User user = createTestUser();
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getLastName(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getBirthDate(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }

    public static UserResponseDto adminUserResponseDto() {
        User admin = createTestAdmin();
        return new UserResponseDto(
                admin.getId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getLastName(),
                admin.getFirstName(),
                admin.getMiddleName(),
                admin.getBirthDate(),
                admin.getRole().name(),
                admin.getCreatedAt()
        );
    }
}