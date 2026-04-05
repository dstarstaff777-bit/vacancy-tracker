package org.example.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.ExperienceLevel;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAiResponse {

    @JsonProperty("skills")
    private List<String> skills;

    @JsonProperty("experience")
    private String experienceString;

    public ExperienceLevel getExperience() {
        if (experienceString == null) {
            return null;
        }
        try {
            return ExperienceLevel.valueOf(experienceString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}