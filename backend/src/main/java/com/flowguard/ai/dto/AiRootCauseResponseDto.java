package com.flowguard.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRootCauseResponseDto {
    private UUID incidentId;
    private String incidentNumber;
    private String summary;
    private Double confidenceScore;
    private List<String> potentialRootCauses;
    private List<String> affectedServices;
    private List<String> relevantErrorPatterns;
    private List<String> recommendedInvestigationSteps;
    private List<String> suggestedRemediationActions;
    private String supportingEvidence;
    private String modelUsed;
    private String disclaimer;
}

