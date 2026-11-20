package com.flowguard.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByTransactionId(String transactionId);
    Page<Transaction> findByTransactionStatus(TransactionStatus status, Pageable pageable);
    Page<Transaction> findByCorrelationId(String correlationId, Pageable pageable);
    Page<Transaction> findByTransactionIdContainingIgnoreCaseOrCorrelationIdContainingIgnoreCase(String txId, String corrId, Pageable pageable);
    long countByTransactionStatus(TransactionStatus status);
}

