package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyMessage {

    private Long vacancyId;
    private String title;
    private String company;
    private String rawText;
    private String sourceUrl;
    private String source;
    private Long salaryFrom;
    private Long salaryTo;
}