package com.flowguard.transaction;

import com.flowguard.common.ApiResponse;
import com.flowguard.transaction.dto.CreateTransactionRequest;
import com.flowguard.transaction.dto.RecordStepRequest;
import com.flowguard.transaction.dto.TransactionDto;
import com.flowguard.transaction.dto.TransactionStepDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Intelligence", description = "Endpoints for distributed multi-service transaction tracing and execution waterfall inspection")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List all distributed transactions with search and status filtering")
    public ResponseEntity<ApiResponse<Page<TransactionDto>>> getAllTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransactionStatus status,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {
        Page<TransactionDto> transactions = transactionService.getAllTransactions(search, status, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Transactions retrieved successfully", transactions));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction details by ID including complete step execution waterfall")
    public ResponseEntity<ApiResponse<TransactionDto>> getTransactionById(@PathVariable UUID id) {
        TransactionDto transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.ok("Transaction retrieved successfully", transaction));
    }

    @GetMapping("/by-txid/{txId}")
    @Operation(summary = "Find transaction by human-readable transaction identifier")
    public ResponseEntity<ApiResponse<TransactionDto>> getTransactionByTxId(@PathVariable String txId) {
        TransactionDto transaction = transactionService.getTransactionByTxId(txId);
        return ResponseEntity.ok(ApiResponse.ok("Transaction retrieved successfully", transaction));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Ingest a new distributed transaction span")
    public ResponseEntity<ApiResponse<TransactionDto>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionDto created = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Transaction registered successfully", created));
    }

    @PostMapping("/{id}/steps")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Append an execution step to a distributed transaction")
    public ResponseEntity<ApiResponse<TransactionStepDto>> recordStep(
            @PathVariable UUID id,
            @Valid @RequestBody RecordStepRequest request) {
        TransactionStepDto step = transactionService.recordStep(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Transaction step recorded successfully", step));
    }
}

