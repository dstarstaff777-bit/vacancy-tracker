package org.example.ai;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    /**
     * Создать промпт для извлечения навыков из текста вакансии
     */
    public String buildSkillsExtractionPrompt(String vacancyText) {

        return """
            Ты — эксперт по анализу вакансий для разработчиков.
            
            Задача: извлечь технические навыки из текста вакансии.
            
            Правила:
            1. Извлекай ТОЛЬКО технические навыки: языки программирования, фреймворки, базы данных, инструменты
            2. НЕ включай soft skills (коммуникабельность, ответственность и тд)
            3. НЕ включай общие слова типа "опыт работы", "знание английского"
            4. Приводи названия к стандартному виду: "spring boot" → "Spring Boot", "postgres" → "PostgreSQL"
            5. Если навык упомянут несколько раз — включи его один раз
            
            Формат ответа: верни ТОЛЬКО JSON, без markdown форматирования, без пояснений.
            Структура JSON:
            {
              "skills": ["Java", "Spring Boot", "PostgreSQL"],
              "experience": "middle",
              "summary": "краткое описание вакансии в 1-2 предложениях"
            }
            
            Уровень опыта (experience) определяй по ключевым словам:
            - "junior" если упоминается: стажёр, junior, начинающий, без опыта, 0-1 год
            - "middle" если: middle, 1-3 года, опыт коммерческой разработки
            - "senior" если: senior, lead, 3+ года, архитектура, менторство
            - "lead" если: team lead, руководитель, управление командой
            
            Пример входа:
            "Требуется Java разработчик. Опыт работы от 2 лет. Стек: Java 17, Spring Boot, PostgreSQL, Docker, Kafka."
            
            Пример выхода:
            {
              "skills": ["Java 17", "Spring Boot", "PostgreSQL", "Docker", "Kafka"],
              "experience": "middle",
              "summary": "Java разработчик с опытом от 2 лет для работы со Spring Boot и микросервисами."
            }
            
            Текст вакансии:
            %s
            """.formatted(vacancyText);


    }


    public String buildResumeSkillsExtractionPrompt(String resumeText) {
        return """
            Ты — эксперт по анализу резюме разработчиков.
            
            Задача: извлечь технические навыки из резюме кандидата.
            
            Правила:
            1. Извлекай ТОЛЬКО те навыки которые кандидат явно указал или упомянул в проектах
            2. НЕ домысливай навыки которых нет в тексте
            3. Приводи к стандартному виду: "react.js" → "React", "mysql" → "MySQL"
            
            Формат ответа: верни ТОЛЬКО JSON без markdown.
            {
              "skills": ["Java", "Spring Boot"],
              "experience": "middle"
            }
            
            Текст резюме:
            %s
            """.formatted(resumeText);
    }
}