package org.example.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MatchResultDto {

    private Long id;
    private Long resumeId;
    private Long vacancyId;
    private String vacancyTitle;
    private String vacancyCompany;
    private Integer score;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private LocalDateTime createdAt;
}