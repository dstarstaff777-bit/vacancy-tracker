package org.example.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ResumeDto;
import org.example.resume.ResumeRequest;
import org.example.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<ResumeDto> createResume(
            @Valid @RequestBody ResumeRequest request
    ) {
        log.info("POST /api/resumes - creating resume for user {}",
                request.getUserId());

        ResumeDto created = resumeService.createResume(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDto> getResumeById(@PathVariable Long id) {
        log.info("GET /api/resumes/{}", id);
        ResumeDto resume = resumeService.getById(id);
        return ResponseEntity.ok(resume);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResumeDto>> getResumesByUserId(
            @PathVariable Long userId
    ) {
        log.info("GET /api/resumes/user/{}", userId);
        List<ResumeDto> resumes = resumeService.getByUserId(userId);
        return ResponseEntity.ok(resumes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeDto> updateResume(
            @PathVariable Long id,
            @RequestBody ResumeRequest request
    ) {
        log.info("PUT /api/resumes/{}", id);
        ResumeDto updated = resumeService.updateResume(id, request);
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id) {
        log.info("DELETE /api/resumes/{}", id);
        resumeService.deleteResume(id);
        return ResponseEntity.noContent().build();

    }
}