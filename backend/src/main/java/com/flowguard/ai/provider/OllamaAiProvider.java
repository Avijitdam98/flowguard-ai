package com.flowguard.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class OllamaAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaAiProvider.class);

    private final String baseUrl;
    private final String model;
    private final int timeoutSeconds;
    private final RestTemplate restTemplate;
    private final RestTemplate healthCheckRestTemplate;
    private final ObjectMapper objectMapper;

    public OllamaAiProvider(
            @Value("${flowguard.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${flowguard.ai.ollama.model:qwen2.5:7b}") String model,
            @Value("${flowguard.ai.ollama.timeout-seconds:30}") int timeoutSeconds,
            ObjectMapper objectMapper) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restTemplate = new RestTemplate(factory);

        SimpleClientHttpRequestFactory healthFactory = new SimpleClientHttpRequestFactory();
        healthFactory.setConnectTimeout(Duration.ofSeconds(2));
        healthFactory.setReadTimeout(Duration.ofSeconds(2));
        this.healthCheckRestTemplate = new RestTemplate(healthFactory);
    }

    @Override
    public String getProviderName() {
        return "Ollama (Local Open-Source)";
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public boolean isAvailable() {
        try {
            ResponseEntity<String> response = healthCheckRestTemplate.getForEntity(baseUrl + "/api/tags", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.debug("Ollama is not available at {}: {}", baseUrl, ex.getMessage());
            return false;
        }
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        String url = baseUrl + "/api/generate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", userPrompt);
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            requestBody.put("system", systemPrompt);
        }
        requestBody.put("stream", false);

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.2);
        requestBody.put("options", options);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("response")) {
                    return root.get("response").asText();
                }
            }
            throw new RuntimeException("Unexpected response from Ollama API: " + response.getStatusCode());
        } catch (Exception ex) {
            log.error("Failed to call Ollama model '{}' at {}: {}", model, baseUrl, ex.getMessage());
            throw new RuntimeException("Ollama generation failed: " + ex.getMessage(), ex);
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}

