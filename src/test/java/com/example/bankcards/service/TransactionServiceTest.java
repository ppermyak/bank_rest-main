package com.example.bankcards.service;

import com.example.bankcards.TestData;
import com.example.bankcards.dto.request.CreateTransactionRequestDto;
import com.example.bankcards.dto.response.TransactionResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private UUID fromCardId;
    private UUID toCardId;

    @BeforeEach
    void setUp() {
        testUser = TestData.createTestUser();
        fromCard = TestData.createTestCard();
        toCard = TestData.createTestCard();
        fromCardId = fromCard.getId();
        toCardId = toCard.getId();

        fromCard.setUser(testUser);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(1000));

        toCard.setUser(testUser);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("Create transaction: Should transfer money when all conditions are met")
    void createTransaction_WhenValid_ShouldTransferMoney() {
        CreateTransactionRequestDto request = TestData.validCreateTransactionRequest(fromCardId, toCardId);
        Transaction transaction = new Transaction();
        TransactionResponseDto expectedResponse = TestData.validTransactionResponseDto(fromCardId, toCardId);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(expectedResponse);

        TransactionResponseDto response = transactionService.createTransaction(request, TestData.TEST_USERNAME);

        assertNotNull(response);
        assertEquals(TestData.TEST_TRANSACTION_AMOUNT, response.amount());
    }

    @Test
    @DisplayName("Create transaction: Should throw CardNotFoundException when from card not found")
    void createTransaction_WhenFromCardNotFound_ShouldThrowCardNotFoundException() {
        CreateTransactionRequestDto request = TestData.validCreateTransactionRequest(fromCardId, toCardId);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request, TestData.TEST_USERNAME))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Create transaction: Should throw CardNotFoundException when to card not found")
    void createTransaction_WhenToCardNotFound_ShouldThrowCardNotFoundException() {
        CreateTransactionRequestDto request = TestData.validCreateTransactionRequest(fromCardId, toCardId);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(request, TestData.TEST_USERNAME))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Get user transactions: Should return page of transactions when user has cards")
    void getUserTransactions_WhenUserHasCards_ShouldReturnPageOfTransactions() {
        Pageable pageable = PageRequest.of(0, 10);
        Transaction transaction = new Transaction();
        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);
        TransactionResponseDto expectedResponse = TestData.validTransactionResponseDto(fromCardId, toCardId);

        when(userRepository.findByUsername(TestData.TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByUser(testUser)).thenReturn(List.of(fromCard, toCard));
        when(transactionRepository.findAllByCards(List.of(fromCard, toCard), pageable)).thenReturn(transactionPage);
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(expectedResponse);

        Page<TransactionResponseDto> response = transactionService.getUserTransactions(TestData.TEST_USERNAME, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Get user transactions: Should throw UserNotFoundException when user not found")
    void getUserTransactions_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getUserTransactions("unknown", pageable))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Get user transactions: Should return empty page when user has no cards")
    void getUserTransactions_WhenUserHasNoCards_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByUsername(TestData.TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(cardRepository.findByUser(testUser)).thenReturn(List.of());

        Page<TransactionResponseDto> response = transactionService.getUserTransactions(TestData.TEST_USERNAME, pageable);

        assertTrue(response.isEmpty());
    }
}