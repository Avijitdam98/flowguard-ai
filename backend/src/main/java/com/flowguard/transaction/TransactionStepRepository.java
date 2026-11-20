package com.flowguard.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionStepRepository extends JpaRepository<TransactionStep, UUID> {
    List<TransactionStep> findByTransactionIdOrderByStepOrderAsc(UUID transactionId);
}

