package com.flowguard.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequestDto {

    @NotBlank(message = "Message prompt cannot be empty")
    private String prompt;

    private List<Map<String, String>> conversationHistory;

    private String contextScope;
}

