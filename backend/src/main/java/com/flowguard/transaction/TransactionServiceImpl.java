package com.flowguard.transaction;

import com.flowguard.common.BadRequestException;
import com.flowguard.common.ResourceNotFoundException;
import com.flowguard.transaction.dto.CreateTransactionRequest;
import com.flowguard.transaction.dto.RecordStepRequest;
import com.flowguard.transaction.dto.TransactionDto;
import com.flowguard.transaction.dto.TransactionStepDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionStepRepository stepRepository;

    @Override
    @Transactional
    public TransactionDto createTransaction(CreateTransactionRequest request) {
        if (transactionRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
            throw new BadRequestException("Transaction with ID '" + request.getTransactionId() + "' already exists");
        }

        Transaction tx = Transaction.builder()
                .transactionId(request.getTransactionId())
                .correlationId(request.getCorrelationId())
                .requestId(request.getRequestId())
                .transactionStatus(request.getTransactionStatus())
                .rootServiceName(request.getRootServiceName())
                .totalDurationMs(request.getTotalDurationMs() != null ? request.getTotalDurationMs() : 0L)
                .failureReason(request.getFailureReason())
                .metadata(request.getMetadata())
                .startTime(Instant.now())
                .build();

        Transaction saved = transactionRepository.save(tx);
        return TransactionDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public TransactionStepDto recordStep(UUID transactionId, RecordStepRequest request) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        TransactionStep step = TransactionStep.builder()
                .transaction(tx)
                .serviceName(request.getServiceName())
                .stepName(request.getStepName())
                .stepOrder(request.getStepOrder())
                .status(request.getStatus())
                .durationMs(request.getDurationMs())
                .errorMessage(request.getErrorMessage())
                .payloadSnapshot(request.getPayloadSnapshot())
                .executedAt(Instant.now())
                .build();

        TransactionStep saved = stepRepository.save(step);
        tx.getSteps().add(saved);

        // Update total duration and failure if step failed
        if (request.getStatus() == StepStatus.FAILED) {
            tx.setTransactionStatus(TransactionStatus.FAILED);
            if (request.getErrorMessage() != null) {
                tx.setFailureReason(request.getErrorMessage());
            }
        }
        tx.setTotalDurationMs(tx.getTotalDurationMs() + request.getDurationMs());
        tx.setEndTime(Instant.now());
        transactionRepository.save(tx);

        return TransactionStepDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(UUID id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return TransactionDto.fromEntity(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDto getTransactionByTxId(String txId) {
        Transaction tx = transactionRepository.findByTransactionId(txId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "transactionId", txId));
        return TransactionDto.fromEntity(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDto> getAllTransactions(String search, TransactionStatus status, Pageable pageable) {
        Page<Transaction> page;
        if (search != null && !search.isBlank()) {
            page = transactionRepository.findByTransactionIdContainingIgnoreCaseOrCorrelationIdContainingIgnoreCase(search, search, pageable);
        } else if (status != null) {
            page = transactionRepository.findByTransactionStatus(status, pageable);
        } else {
            page = transactionRepository.findAll(pageable);
        }
        return page.map(TransactionDto::fromEntity);
    }
}

