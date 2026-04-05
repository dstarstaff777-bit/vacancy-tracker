package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.VacancyDto;
import org.example.entity.VacancyStatus;
import org.example.service.VacancyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;


    @GetMapping("/{id}")
    public ResponseEntity<VacancyDto> getVacancyById(
            @PathVariable Long id
    ) {
        log.info("GET /api/vacancies/{}", id);
        VacancyDto vacancy = vacancyService.getById(id);
        return ResponseEntity.ok(vacancy);

    }

    @GetMapping
    public ResponseEntity<List<VacancyDto>> getVacancies(
            @RequestParam(required = false) VacancyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("GET /api/vacancies?status={}&page={}&size={}",
                status, page, size);
        List<VacancyDto> vacancies = vacancyService.getVacancies(
                status, page, size
        );

        return ResponseEntity.ok(vacancies);
    }

    @PostMapping("/parse")
    public ResponseEntity<String> triggerParsing() {
        log.info("POST /api/vacancies/parse - manual parsing triggered");

        // TODO: добавить вызов ParserService.parseAndSendToKafka()
        // но это будет долгая операция, лучше делать асинхронно

        return ResponseEntity.ok("Parsing started");
    }

    @GetMapping("/stuck")
    public ResponseEntity<List<VacancyDto>> getStuckVacancies(
            @RequestParam(defaultValue = "2") int hoursThreshold
    ) {
        log.info("GET /api/vacancies/stuck?hours={}", hoursThreshold);
        List<VacancyDto> stuck = vacancyService.findStuckVacancies(
                hoursThreshold
        );

        return ResponseEntity.ok(stuck);
    }
}