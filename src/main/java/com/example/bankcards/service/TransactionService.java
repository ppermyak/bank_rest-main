package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateTransactionRequestDto;
import com.example.bankcards.dto.response.TransactionResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransactionStatus;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidCardStatusException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponseDto createTransaction(CreateTransactionRequestDto request, String username) {
        Card fromCard = cardRepository.findById(request.fromCardId())
                .orElseThrow(() -> new CardNotFoundException("From card not found with id: " + request.fromCardId()));

        Card toCard = cardRepository.findById(request.toCardId())
                .orElseThrow(() -> new CardNotFoundException("To card not found with id: " + request.toCardId()));

        if (!fromCard.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not the owner of the from card");
        }

        if (!toCard.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not the owner of the to card");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardStatusException("From card is not active. Current status: " + fromCard.getStatus());
        }

        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidCardStatusException("To card is not active. Current status: " + toCard.getStatus());
        }

        if (fromCard.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Available: " + fromCard.getBalance() + ", Requested: " + request.amount());
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
        toCard.setBalance(toCard.getBalance().add(request.amount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(request.amount());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCreatedAt(LocalDateTime.now());

        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.toResponseDto(saved);
    }

    public Page<TransactionResponseDto> getUserTransactions(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        List<Card> cards = cardRepository.findByUser(user);

        if (cards.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Transaction> transactionsPage = transactionRepository.findAllByCards(cards, pageable);

        return transactionsPage.map(transactionMapper::toResponseDto);
    }
}