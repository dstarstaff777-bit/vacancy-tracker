package org.example.matching;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScoreCalculator {

    public int calculateScore(
            List<String> resumeSkills,
            List<String> vacancySkills
    ) {
        // Проверки на null и пустые списки
        if (resumeSkills == null || resumeSkills.isEmpty()) {
            log.warn("Resume skills are empty");
            return 0;
        }

        if (vacancySkills == null || vacancySkills.isEmpty()) {
            log.warn("Vacancy skills are empty");
            return 0;
        }

        Set<String> normalizedResumeSkills = resumeSkills.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> normalizedVacancySkills = vacancySkills.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> intersection = new HashSet<>(normalizedResumeSkills);
        intersection.retainAll(normalizedVacancySkills);

        double matchPercentage = (double) intersection.size() /
                normalizedVacancySkills.size();

        int baseScore = (int) (matchPercentage * 100);
        int extraSkills = normalizedResumeSkills.size() - intersection.size();
        int bonus = Math.min(extraSkills * 2, 10);

        int finalScore = Math.min(baseScore + bonus, 100);

        log.debug("Score calculation: matched={}/{}, extra={}, score={}",
                intersection.size(),
                normalizedVacancySkills.size(),
                extraSkills,
                finalScore);

        return finalScore;
    }

    public List<String> findMatchedSkills(
            List<String> resumeSkills,
            List<String> vacancySkills
    ) {
        if (resumeSkills == null || vacancySkills == null) {
            return List.of();
        }

        Set<String> normalizedResumeSkills = resumeSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return vacancySkills.stream()
                .filter(skill -> normalizedResumeSkills.contains(
                        skill.toLowerCase()
                ))
                .collect(Collectors.toList());
    }

    public List<String> findMissingSkills(
            List<String> resumeSkills,
            List<String> vacancySkills
    ) {
        if (vacancySkills == null) {
            return List.of();
        }

        if (resumeSkills == null) {
            return new ArrayList<>(vacancySkills);
        }

        Set<String> normalizedResumeSkills = resumeSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return vacancySkills.stream()
                .filter(skill -> !normalizedResumeSkills.contains(
                        skill.toLowerCase()
                ))
                .collect(Collectors.toList());
    }

    public int calculateScoreWithFuzzy(
            List<String> resumeSkills,
            List<String> vacancySkills
    ) {

        return calculateScore(resumeSkills, vacancySkills);
    }
}
