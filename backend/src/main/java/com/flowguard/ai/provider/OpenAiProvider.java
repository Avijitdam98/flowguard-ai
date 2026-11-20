package com.flowguard.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);

    private final String apiKey;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAiProvider(
            @Value("${flowguard.ai.openai.api-key:${OPENAI_API_KEY:}}") String apiKey,
            @Value("${flowguard.ai.openai.model:gpt-4o-mini}") String model,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String getProviderName() {
        return "OpenAI (Cloud)";
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public boolean isAvailable() {
        return !apiKey.isBlank() && !apiKey.contains("your_openai_api_key_here");
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured.");
        }

        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userPrompt));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.2);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                    return choices.get(0).get("message").get("content").asText();
                }
            }
            throw new RuntimeException("Unexpected response from OpenAI API: " + response.getStatusCode());
        } catch (Exception ex) {
            log.error("Failed to call OpenAI API: {}", ex.getMessage());
            throw new RuntimeException("OpenAI generation failed: " + ex.getMessage(), ex);
        }
    }
}

