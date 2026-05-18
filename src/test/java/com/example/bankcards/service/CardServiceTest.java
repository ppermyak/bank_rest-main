package com.example.bankcards.service;

import com.example.bankcards.TestData;
import com.example.bankcards.dto.request.CreateCardRequestDto;
import com.example.bankcards.dto.response.CardBalanceResponseDto;
import com.example.bankcards.dto.response.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InvalidCardStatusException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Tests")
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;
    private UUID testCardId;

    @BeforeEach
    void setUp() {
        testUser = TestData.createTestUser();
        testCard = TestData.createTestCard();
        testCardId = testCard.getId();
    }

    @Test
    @DisplayName("Create card: Should return CardResponseDto when user exists")
    void createCard_WhenUserExists_ShouldReturnCardResponseDto() {
        CreateCardRequestDto dto = TestData.validCreateCardRequest();

        when(userRepository.findByUsername(TestData.TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(encryptionUtil.encrypt(TestData.TEST_CARD_NUMBER)).thenReturn(TestData.TEST_ENCRYPTED_CARD_NUMBER);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toResponseDto(any(Card.class))).thenReturn(TestData.activeCardResponse());

        CardResponseDto response = cardService.createCard(dto);

        assertNotNull(response);
        assertEquals(TestData.TEST_CARD_MASKED, response.cardNumberMasked());
        assertEquals(CardStatus.ACTIVE.name(), response.status());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Create card: Should throw UserNotFoundException when user not found")
    void createCard_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        CreateCardRequestDto dto = new CreateCardRequestDto(
                "unknown",
                TestData.TEST_CARD_NUMBER,
                TestData.TEST_CARD_EXPIRY_DATE
        );

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: unknown");

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Get all cards: Should return page of cards")
    void getAllCards_ShouldReturnPageOfCards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard), pageable, 1);

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toResponseDto(any(Card.class))).thenReturn(TestData.activeCardResponse());

        Page<CardResponseDto> response = cardService.getAllCards(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(TestData.TEST_CARD_MASKED, response.getContent().get(0).cardNumberMasked());
    }

    @Test
    @DisplayName("Delete card: Should delete card when it exists")
    void deleteCard_WhenCardExists_ShouldDeleteCard() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        cardService.deleteCard(testCardId);

        verify(cardRepository).delete(testCard);
    }

    @Test
    @DisplayName("Delete card: Should throw CardNotFoundException when card not found")
    void deleteCard_WhenCardNotFound_ShouldThrowCardNotFoundException() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(testCardId))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Card not found with id: " + testCardId);

        verify(cardRepository, never()).delete(any(Card.class));
    }

    @Test
    @DisplayName("Get card balance: Should return balance when user is owner")
    void getCardBalance_WhenUserIsOwner_ShouldReturnBalance() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        CardBalanceResponseDto response = cardService.getCardBalance(testCardId, TestData.TEST_USERNAME);

        assertNotNull(response);
        assertEquals(TestData.TEST_CARD_BALANCE, response.balance());
    }

    @Test
    @DisplayName("Get card balance: Should throw AccessDeniedException when user is not owner")
    void getCardBalance_WhenUserIsNotOwner_ShouldThrowAccessDeniedException() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        assertThatThrownBy(() -> cardService.getCardBalance(testCardId, "wrong_user"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not the owner of this card");
    }

    @Test
    @DisplayName("Get card balance: Should throw CardNotFoundException when card not found")
    void getCardBalance_WhenCardNotFound_ShouldThrowCardNotFoundException() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardBalance(testCardId, TestData.TEST_USERNAME))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Request block card: Should set status to PENDING_BLOCK when user is owner")
    void requestBlockCard_WhenUserIsOwner_ShouldSetPendingBlock() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toResponseDto(any(Card.class))).thenReturn(TestData.pendingBlockCardResponse());

        CardResponseDto response = cardService.requestBlockCard(testCardId, TestData.TEST_USERNAME);

        assertEquals(CardStatus.PENDING_BLOCK.name(), response.status());
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Request block card: Should throw AccessDeniedException when user is not owner")
    void requestBlockCard_WhenUserIsNotOwner_ShouldThrowAccessDeniedException() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        assertThatThrownBy(() -> cardService.requestBlockCard(testCardId, "wrong_user"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Request block card: Should throw CardNotFoundException when card not found")
    void requestBlockCard_WhenCardNotFound_ShouldThrowCardNotFoundException() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.requestBlockCard(testCardId, TestData.TEST_USERNAME))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Block card: Should block card when status is PENDING_BLOCK")
    void blockCard_WhenStatusIsPendingBlock_ShouldBlockCard() {
        Card pendingBlockCard = TestData.createTestCardWithStatus(CardStatus.PENDING_BLOCK);

        when(cardRepository.findById(pendingBlockCard.getId())).thenReturn(Optional.of(pendingBlockCard));
        when(cardRepository.save(any(Card.class))).thenReturn(pendingBlockCard);
        when(cardMapper.toResponseDto(any(Card.class))).thenReturn(TestData.blockedCardResponse());

        CardResponseDto response = cardService.blockCard(pendingBlockCard.getId());

        assertEquals(CardStatus.BLOCKED.name(), response.status());
    }

    @Test
    @DisplayName("Block card: Should throw InvalidCardStatusException when status is not PENDING_BLOCK")
    void blockCard_WhenStatusIsNotPendingBlock_ShouldThrowInvalidCardStatusException() {
        Card activeCard = TestData.createTestCardWithStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(activeCard.getId())).thenReturn(Optional.of(activeCard));

        assertThatThrownBy(() -> cardService.blockCard(activeCard.getId()))
                .isInstanceOf(InvalidCardStatusException.class)
                .hasMessageContaining("Block request not found for this card");
    }

    @Test
    @DisplayName("Block card: Should throw CardNotFoundException when card not found")
    void blockCard_WhenCardNotFound_ShouldThrowCardNotFoundException() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.blockCard(testCardId))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Update card status: Should update status when card exists")
    void updateCardStatus_WhenCardExists_ShouldUpdateStatus() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toResponseDto(any(Card.class))).thenReturn(TestData.blockedCardResponse());

        CardResponseDto response = cardService.updateCardStatus(testCardId, "BLOCKED");

        assertEquals(CardStatus.BLOCKED.name(), response.status());
    }

    @Test
    @DisplayName("Update card status: Should throw InvalidCardStatusException when status is invalid")
    void updateCardStatus_WhenStatusInvalid_ShouldThrowInvalidCardStatusException() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        assertThatThrownBy(() -> cardService.updateCardStatus(testCardId, "INVALID"))
                .isInstanceOf(InvalidCardStatusException.class)
                .hasMessageContaining("Invalid status: INVALID");
    }

    @Test
    @DisplayName("Update card status: Should throw CardNotFoundException when card not found")
    void updateCardStatus_WhenCardNotFound_ShouldThrowCardNotFoundException() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateCardStatus(testCardId, "ACTIVE"))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Get user cards: Should return page of user's cards")
    void getUserCards_ShouldReturnPageOfUserCards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard), pageable, 1);

        when(userRepository.findByUsername(TestData.TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByUser(testUser, pageable)).thenReturn(cardPage);
        when(cardMapper.toResponseDto(any(Card.class))).thenReturn(TestData.activeCardResponse());

        Page<CardResponseDto> response = cardService.getUserCards(TestData.TEST_USERNAME, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Get user cards: Should throw UserNotFoundException when user not found")
    void getUserCards_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getUserCards("unknown", pageable))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: unknown");
    }

    @Test
    @DisplayName("Get user cards: Should return empty page when user has no cards")
    void getUserCards_WhenUserHasNoCards_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> emptyPage = Page.empty(pageable);

        when(userRepository.findByUsername(TestData.TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByUser(testUser, pageable)).thenReturn(emptyPage);

        Page<CardResponseDto> response = cardService.getUserCards(TestData.TEST_USERNAME, pageable);

        assertTrue(response.isEmpty());
    }
}