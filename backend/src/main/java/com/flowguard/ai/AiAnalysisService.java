package com.flowguard.ai;

import com.flowguard.ai.dto.AiChatRequestDto;
import com.flowguard.ai.dto.AiChatResponseDto;
import com.flowguard.ai.dto.AiRootCauseResponseDto;

import java.util.UUID;

public interface AiAnalysisService {
    AiRootCauseResponseDto analyzeIncident(UUID incidentId);
    AiChatResponseDto chatWithCopilot(AiChatRequestDto request);
}

