package org.example.config;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.Response;

import java.util.List;
import java.util.Set;

public class DummyChatLanguageModel implements ChatLanguageModel {

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        String content = extractLastMessageContent(messages);
        return Response.from(AiMessage.from(generateResponse(content)));
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications) {
        return generate(messages);
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, ToolSpecification toolSpecification) {
        return generate(messages);
    }

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        String content = extractLastMessageContent(chatRequest.messages());
        return ChatResponse.builder()
                .aiMessage(AiMessage.from(generateResponse(content)))
                .build();
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        return Set.of();
    }

    private String extractLastMessageContent(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        return lastMessage.text();
    }

    private String generateResponse(String prompt) {
        if (prompt.contains("вакансия") || prompt.contains("vacancy") || prompt.contains("навыки")) {
            return """
                    {
                      "skills": ["Java", "Spring", "SQL", "Docker"],
                      "experienceString": "3-5 лет",
                      "summary": "Заглушка: AI не настроен"
                    }
                    """;
        } else if (prompt.contains("резюме") || prompt.contains("resume")) {
            return """
                    {
                      "skills": ["Java", "Spring Boot", "Microservices"],
                      "experienceString": "5 лет"
                    }
                    """;
        } else {
            return """
                    {
                      "skills": [],
                      "experienceString": null,
                      "summary": "Заглушка: AI не настроен"
                    }
                    """;
        }
    }
}