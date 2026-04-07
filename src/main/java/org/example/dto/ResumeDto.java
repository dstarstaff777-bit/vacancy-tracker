package org.example.dto;

import lombok.Builder;
import lombok.Data;
import org.example.entity.ExperienceLevel;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ResumeDto {
    private Long id;
    private Long userId;
    private List<String> skills;
    private ExperienceLevel experience;
    private LocalDateTime createdAt;

}