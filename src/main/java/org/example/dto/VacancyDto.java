package org.example.dto;

import lombok.Builder;
import lombok.Data;
import org.example.entity.ExperienceLevel;
import org.example.entity.VacancyStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VacancyDto {

    private Long id;
    private String title;
    private String company;
    private List<String> skills;
    private Long salaryFrom;
    private Long salaryTo;
    private ExperienceLevel experience;
    private String source;
    private String sourceUrl;
    private VacancyStatus status;
    private LocalDateTime parsedAt;

}