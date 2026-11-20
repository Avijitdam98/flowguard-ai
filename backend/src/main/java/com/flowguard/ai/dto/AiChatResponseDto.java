package com.flowguard.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponseDto {
    private String response;
    private String model;
    private List<String> referencedEntities;
    private List<String> suggestedNextPrompts;
    private Instant generatedAt;
    private String safetyNotice;
}

