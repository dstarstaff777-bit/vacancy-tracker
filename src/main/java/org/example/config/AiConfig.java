package org.example.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.model:gpt-3.5-turbo}")
    private String modelName;

    @Value("${spring.ai.openai.timeout:60s}")
    private Duration timeout;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // Если ключ не задан, используем заглушку
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("⚠️ OpenAI API key not set, using dummy ChatLanguageModel (returns placeholder responses)");
            return new DummyChatLanguageModel();
        }

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.0)
                .maxTokens(1000)
                .timeout(timeout)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
