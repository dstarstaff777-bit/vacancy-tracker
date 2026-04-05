package org.example.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.dto.ResumeAiResponse;
import org.example.ai.dto.VacancyAiResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatLanguageModel chatModel;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Cacheable(value = "ai-skills-extraction", key = "#vacancyText.hashCode()")
    public VacancyAiResponse extractSkillsFromVacancy(String vacancyText) {
        log.debug("Extracting skills from vacancy text (length: {})",
                vacancyText.length());

        try {
            String prompt = promptBuilder.buildSkillsExtractionPrompt(vacancyText);
            long startTime = System.currentTimeMillis();
            String aiResponse = chatModel.generate(prompt);
            long duration = System.currentTimeMillis() - startTime;

            log.info("AI responded in {} ms", duration);
            log.debug("AI response: {}", aiResponse);

            VacancyAiResponse response = parseAiResponse(aiResponse);

            log.info("Extracted {} skills: {}",
                    response.getSkills().size(),
                    response.getSkills());

            return response;

        } catch (Exception e) {
            log.error("Error extracting skills from vacancy", e);
            // Если AI упал — возвращаем пустой результат
            // лучше чем роняться полностью
            return VacancyAiResponse.builder()
                    .skills(List.of())
                    .experienceString(null)
                    .summary("Ошибка обработки")
                    .build();
        }
    }

    @Cacheable(value = "ai-resume-extraction", key = "#resumeText.hashCode()")
    public ResumeAiResponse extractSkillsFromResume(String resumeText) {
        log.debug("Extracting skills from resume");

        try {
            String prompt = promptBuilder.buildResumeSkillsExtractionPrompt(resumeText);
            String aiResponse = chatModel.generate(prompt);

            // Парсим ответ
            return objectMapper.readValue(
                    cleanJsonResponse(aiResponse),
                    ResumeAiResponse.class
            );

        } catch (Exception e) {
            log.error("Error extracting skills from resume", e);
            return ResumeAiResponse.builder()
                    .skills(List.of())
                    .experienceString(null)
                    .build();
        }
    }


    private VacancyAiResponse parseAiResponse(String aiResponse)
            throws JsonProcessingException {

        String cleaned = cleanJsonResponse(aiResponse);

        return objectMapper.readValue(cleaned, VacancyAiResponse.class);
    }

    private String cleanJsonResponse(String response) {
        String cleaned = response
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

        if (!cleaned.startsWith("{")) {
            int startIndex = cleaned.indexOf("{");
            if (startIndex != -1) {
                cleaned = cleaned.substring(startIndex);
            }
        }

        return cleaned;
    }
    public String extractSkills(String text) {
        VacancyAiResponse response = extractSkillsFromVacancy(text);
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Error serializing AI response", e);
            return "{}";
        }
    }
}