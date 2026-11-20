package com.flowguard.transaction;

import com.flowguard.transaction.dto.CreateTransactionRequest;
import com.flowguard.transaction.dto.RecordStepRequest;
import com.flowguard.transaction.dto.TransactionDto;
import com.flowguard.transaction.dto.TransactionStepDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {
    TransactionDto createTransaction(CreateTransactionRequest request);
    TransactionStepDto recordStep(UUID transactionId, RecordStepRequest request);
    TransactionDto getTransactionById(UUID id);
    TransactionDto getTransactionByTxId(String txId);
    Page<TransactionDto> getAllTransactions(String search, TransactionStatus status, Pageable pageable);
}

