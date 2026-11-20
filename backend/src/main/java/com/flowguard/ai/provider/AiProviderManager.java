package com.flowguard.ai.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AiProviderManager {

    private static final Logger log = LoggerFactory.getLogger(AiProviderManager.class);

    private final OllamaAiProvider ollamaProvider;
    private final OpenAiProvider openAiProvider;
    private final String preferredProvider;

    public AiProviderManager(
            OllamaAiProvider ollamaProvider,
            OpenAiProvider openAiProvider,
            @Value("${flowguard.ai.provider:ollama}") String preferredProvider) {
        this.ollamaProvider = ollamaProvider;
        this.openAiProvider = openAiProvider;
        this.preferredProvider = preferredProvider;
    }

    public Optional<AiProvider> getActiveProvider() {
        if ("ollama".equalsIgnoreCase(preferredProvider)) {
            if (ollamaProvider.isAvailable()) {
                return Optional.of(ollamaProvider);
            }
            if (openAiProvider.isAvailable()) {
                log.info("Ollama is offline, falling back to OpenAI");
                return Optional.of(openAiProvider);
            }
        } else if ("openai".equalsIgnoreCase(preferredProvider)) {
            if (openAiProvider.isAvailable()) {
                return Optional.of(openAiProvider);
            }
            if (ollamaProvider.isAvailable()) {
                log.info("OpenAI is not configured, falling back to local Ollama");
                return Optional.of(ollamaProvider);
            }
        }
        return Optional.empty();
    }

    public OllamaAiProvider getOllamaProvider() {
        return ollamaProvider;
    }

    public OpenAiProvider getOpenAiProvider() {
        return openAiProvider;
    }
}

