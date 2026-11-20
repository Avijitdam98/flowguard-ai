package com.flowguard.logmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchIngestLogRequest {

    @NotEmpty(message = "Log entries list cannot be empty")
    @Valid
    private List<IngestLogRequest> logs;
}

