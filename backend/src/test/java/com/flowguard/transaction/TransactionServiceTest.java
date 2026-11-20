package com.flowguard.transaction;

import com.flowguard.transaction.dto.CreateTransactionRequest;
import com.flowguard.transaction.dto.RecordStepRequest;
import com.flowguard.transaction.dto.TransactionDto;
import com.flowguard.transaction.dto.TransactionStepDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionStepRepository stepRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UUID transactionId;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        sampleTransaction = Transaction.builder()
                .id(transactionId)
                .transactionId("TXN-TEST-001")
                .correlationId("CORR-ABC")
                .transactionStatus(TransactionStatus.PROCESSING)
                .rootServiceName("api-gateway")
                .totalDurationMs(0L)
                .startTime(Instant.now())
                .steps(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void testCreateTransaction() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .transactionId("TXN-TEST-001")
                .correlationId("CORR-ABC")
                .transactionStatus(TransactionStatus.PROCESSING)
                .rootServiceName("api-gateway")
                .build();

        when(transactionRepository.findByTransactionId("TXN-TEST-001")).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        TransactionDto result = transactionService.createTransaction(request);

        assertNotNull(result);
        assertEquals("TXN-TEST-001", result.getTransactionId());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should record step and update transaction total duration")
    void testRecordStep() {
        RecordStepRequest stepRequest = RecordStepRequest.builder()
                .serviceName("order-service")
                .stepName("Reserve Inventory")
                .stepOrder(1)
                .status(StepStatus.SUCCESS)
                .durationMs(85)
                .build();

        TransactionStep stepEntity = TransactionStep.builder()
                .id(UUID.randomUUID())
                .transaction(sampleTransaction)
                .serviceName("order-service")
                .stepName("Reserve Inventory")
                .stepOrder(1)
                .status(StepStatus.SUCCESS)
                .durationMs(85)
                .executedAt(Instant.now())
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(sampleTransaction));
        when(stepRepository.save(any(TransactionStep.class))).thenReturn(stepEntity);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        TransactionStepDto stepDto = transactionService.recordStep(transactionId, stepRequest);

        assertNotNull(stepDto);
        assertEquals("Reserve Inventory", stepDto.getStepName());
        assertEquals(85, stepDto.getDurationMs());
        assertEquals(85L, sampleTransaction.getTotalDurationMs());
    }
}

