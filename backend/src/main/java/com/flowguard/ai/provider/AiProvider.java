package com.flowguard.ai.provider;

public interface AiProvider {
    String getProviderName();
    String getModelName();
    boolean isAvailable();
    String generate(String systemPrompt, String userPrompt);
}

