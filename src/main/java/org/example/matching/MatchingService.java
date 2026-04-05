package org.example.matching;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.MatchResultDto;
import org.example.entity.MatchResult;
import org.example.entity.Resume;
import org.example.entity.Vacancy;
import org.example.entity.VacancyStatus;
import org.example.exception.ResumeNotFoundException;
import org.example.repository.MatchResultRepository;
import org.example.repository.ResumeRepository;
import org.example.repository.VacancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final ResumeRepository resumeRepository;
    private final VacancyRepository vacancyRepository;
    private final MatchResultRepository matchResultRepository;
    private final ScoreCalculator scoreCalculator;

    @Transactional
    public List<MatchResultDto> matchResumeWithVacancies(Long resumeId) {
        log.info("Starting matching for resume {}", resumeId);

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException(
                        "Resume not found with id: " + resumeId
                ));

        if (resume.getSkills() == null || resume.getSkills().isEmpty()) {
            log.warn("Resume {} has no skills, cannot match", resumeId);
            return List.of();
        }

        matchResultRepository.deleteByResumeId(resumeId);
        log.debug("Deleted old match results for resume {}", resumeId);

        List<Vacancy> vacancies = vacancyRepository.findByStatus(
                VacancyStatus.PROCESSED
        );

        log.info("Found {} processed vacancies to match", vacancies.size());

        List<MatchResult> matchResults = vacancies.stream()
                .map(vacancy -> matchResumeWithVacancy(resume, vacancy))
                .filter(result -> result.getScore() > 0)
                .collect(Collectors.toList());

        // saveAll возвращает List<MatchResult> с заполненными ID
        List<MatchResult> savedResults = matchResultRepository.saveAll(matchResults);

        log.info("Saved {} match results for resume {}",
                savedResults.size(), resumeId);

        return savedResults.stream()
                .map(this::mapToDto)
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    private MatchResult matchResumeWithVacancy(Resume resume, Vacancy vacancy) {
        log.debug("Matching resume {} with vacancy {}",
                resume.getId(), vacancy.getId());

        if (vacancy.getSkills() == null || vacancy.getSkills().isEmpty()) {
            log.warn("Vacancy {} has no skills", vacancy.getId());
            return createEmptyMatchResult(resume, vacancy);
        }

        int score = scoreCalculator.calculateScore(
                resume.getSkills(),
                vacancy.getSkills()
        );

        List<String> matchedSkills = scoreCalculator.findMatchedSkills(
                resume.getSkills(),
                vacancy.getSkills()
        );

        List<String> missingSkills = scoreCalculator.findMissingSkills(
                resume.getSkills(),
                vacancy.getSkills()
        );

        return MatchResult.builder()
                .resume(resume)
                .vacancy(vacancy)
                .score(score)
                .matchedSkills(matchedSkills)
                .missingSkills(missingSkills)
                .build();
    }

    private MatchResult createEmptyMatchResult(Resume resume, Vacancy vacancy) {
        return MatchResult.builder()
                .resume(resume)
                .vacancy(vacancy)
                .score(0)
                .matchedSkills(List.of())
                .missingSkills(vacancy.getSkills() != null
                        ? vacancy.getSkills()
                        : List.of())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MatchResultDto> getMatchResults(Long resumeId) {
        log.debug("Getting match results for resume {}", resumeId);

        if (!resumeRepository.existsById(resumeId)) {
            throw new ResumeNotFoundException(
                    "Resume not found with id: " + resumeId
            );
        }

        List<MatchResult> results = matchResultRepository
                .findByResumeIdOrderByScoreDesc(resumeId);

        if (results.isEmpty()) {
            log.info("No cached match results for resume {}, running matching",
                    resumeId);
            return matchResumeWithVacancies(resumeId);
        }

        log.debug("Found {} cached match results for resume {}",
                results.size(), resumeId);

        return results.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchResultDto> getMatchResultsAboveThreshold(
            Long resumeId,
            int minScore
    ) {
        log.debug("Getting match results for resume {} with minScore {}",
                resumeId, minScore);

        List<MatchResult> results = matchResultRepository
                .findByResume_IdAndScoreGreaterThanEqual(resumeId, minScore);

        return results.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private MatchResultDto mapToDto(MatchResult matchResult) {
        return MatchResultDto.builder()
                .id(matchResult.getId())
                .resumeId(matchResult.getResume().getId())
                .vacancyId(matchResult.getVacancy().getId())
                .vacancyTitle(matchResult.getVacancy().getTitle())
                .vacancyCompany(matchResult.getVacancy().getCompany())
                .score(matchResult.getScore())
                .matchedSkills(matchResult.getMatchedSkills())
                .missingSkills(matchResult.getMissingSkills())
                .createdAt(matchResult.getCreatedAt())
                .build();
    }
}