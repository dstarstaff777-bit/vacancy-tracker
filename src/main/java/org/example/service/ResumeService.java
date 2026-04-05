package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.AiService;
import org.example.ai.dto.ResumeAiResponse;
import org.example.dto.ResumeDto;
import org.example.entity.Resume;
import org.example.exception.ResumeNotFoundException;
import org.example.repository.ResumeRepository;
import org.example.resume.ResumeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final AiService aiService;

    @Transactional
    public ResumeDto createResume(ResumeRequest request) {
        log.info("Creating resume for user {}", request.getUserId());

        ResumeAiResponse aiResponse = aiService
                .extractSkillsFromResume(request.getRawText());

        Resume resume = Resume.builder()
                .userId(request.getUserId())
                .rawText(request.getRawText())
                .skills(aiResponse.getSkills())
                .experience(aiResponse.getExperience())
                .build();

        resume = resumeRepository.save(resume);

        log.info("Resume created with id: {}", resume.getId());

        return mapToDto(resume);
    }

    @Transactional(readOnly = true)
    public ResumeDto getById(Long id) {
        log.debug("Getting resume by id: {}", id);

        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException(
                        "Resume not found with id: " + id
                ));

        return mapToDto(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeDto> getByUserId(Long userId) {
        log.debug("Getting resumes for user {}", userId);

        List<Resume> resumes = resumeRepository.findByUserId(userId);

        return resumes.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResumeDto updateResume(Long id, ResumeRequest request) {
        log.info("Updating resume {}", id);

        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException(
                        "Resume not found with id: " + id
                ));

        if (!resume.getRawText().equals(request.getRawText())) {
            log.info("Resume text changed, re-extracting skills");

            ResumeAiResponse aiResponse = aiService
                    .extractSkillsFromResume(request.getRawText());

            resume.setRawText(request.getRawText());
            resume.setSkills(aiResponse.getSkills());
            resume.setExperience(aiResponse.getExperience());
        }

        return mapToDto(resume);
    }

    @Transactional
    public void deleteResume(Long id) {
        log.info("Deleting resume {}", id);

        if (!resumeRepository.existsById(id)) {
            throw new ResumeNotFoundException(
                    "Resume not found with id: " + id
            );
        }

        resumeRepository.deleteById(id);
    }

    private ResumeDto mapToDto(Resume resume) {
        return ResumeDto.builder()
                .id(resume.getId())
                .userId(resume.getUserId())
                .skills(resume.getSkills())
                .experience(resume.getExperience())
                .createdAt(resume.getCreatedAt())
                .build();
    }
}