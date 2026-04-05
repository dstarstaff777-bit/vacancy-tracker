package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.MatchResultDto;
import org.example.matching.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;


    @PostMapping("/resume/{resumeId}")
    public ResponseEntity<List<MatchResultDto>> matchResume(
            @PathVariable Long resumeId
    ) {
        log.info("POST /api/matching/resume/{} - starting matching", resumeId);

        List<MatchResultDto> results = matchingService
                .matchResumeWithVacancies(resumeId);

        log.info("Matching completed, found {} matches", results.size());

        return ResponseEntity.ok(results);
    }

    @GetMapping("/resume/{resumeId}")
    public ResponseEntity<List<MatchResultDto>> getMatchResults(
            @PathVariable Long resumeId,
            @RequestParam(required = false) Integer minScore

    ) {
        log.info("GET /api/matching/resume/{}?minScore={}",
                resumeId, minScore);

        List<MatchResultDto> results;

        if (minScore != null && minScore > 0) {
            results = matchingService.getMatchResultsAboveThreshold(
                    resumeId, minScore
            );
        } else {
            results = matchingService.getMatchResults(resumeId);
        }

        return ResponseEntity.ok(results);
    }
}