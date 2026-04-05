package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.VacancyDto;
import org.example.entity.VacancyStatus;
import org.example.repository.VacancyRepository;
import org.example.entity.Vacancy;
import org.example.exception.VacancyNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyService {

    private final VacancyRepository vacancyRepository;

    @Transactional(readOnly = true)
    public VacancyDto getById(Long id) {
        log.debug("Getting vacancy by id: {}", id);
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException(
                        "Vacancy not found with id: " + id
                ));

        return mapToDto(vacancy);
    }

    @Transactional(readOnly = true)
    public List<VacancyDto> getVacancies(
            VacancyStatus status,
            int page,
            int size
    ) {
        log.debug("Getting vacancies with status: {}, page: {}, size: {}",
                status, page, size);

        List<Vacancy> vacancies;

        if (status != null) {
            vacancies = vacancyRepository.findByStatusOrderByParsedAtDesc(
                    status,
                    PageRequest.of(page, size)
            );
        } else {
            vacancies = vacancyRepository.findAll(
                    PageRequest.of(page, size)
            ).getContent();
        }

        return vacancies.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public VacancyDto save(Vacancy vacancy) {
        log.debug("Saving vacancy: {}", vacancy.getTitle());
        Vacancy saved = vacancyRepository.save(vacancy);
        log.info("Vacancy saved with id: {}", saved.getId());
        return mapToDto(saved);
    }

    @Transactional
    public void updateStatus(Long id, VacancyStatus newStatus) {
        log.debug("Updating vacancy {} status to {}", id, newStatus);

        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException(
                        "Vacancy not found with id: " + id
                ));

        vacancy.setStatus(newStatus);

        log.info("Vacancy {} status updated to {}", id, newStatus);
    }

    @Transactional(readOnly = true)
    public List<VacancyDto> findStuckVacancies(int hoursThreshold) {
        log.debug("Finding stuck vacancies older than {} hours", hoursThreshold);

        LocalDateTime before = LocalDateTime.now()
                .minusHours(hoursThreshold);

        List<Vacancy> stuck = vacancyRepository.findStuckVacancies(
                VacancyStatus.PROCESSING,
                before
        );

        log.info("Found {} stuck vacancies", stuck.size());

        return stuck.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private VacancyDto mapToDto(Vacancy vacancy) {
        return VacancyDto.builder()
                .id(vacancy.getId())
                .title(vacancy.getTitle())
                .company(vacancy.getCompany())
                .skills(vacancy.getSkills())
                .salaryFrom(vacancy.getSalaryFrom())
                .salaryTo(vacancy.getSalaryTo())
                .experience(vacancy.getExperience())
                .source(vacancy.getSource())
                .sourceUrl(vacancy.getSourceUrl())
                .status(vacancy.getStatus())
                .parsedAt(vacancy.getParsedAt())
                .build();
    }
}
