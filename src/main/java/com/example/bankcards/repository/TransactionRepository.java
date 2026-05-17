package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("SELECT DISTINCT t FROM Transaction t " +
            "WHERE t.fromCard IN :cards OR t.toCard IN :cards " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findAllByCards(@Param("cards") List<Card> cards, Pageable pageable);
}