package org.example.parser;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParsedVacancy {

    private String title;
    private String company;
    private String sourceUrl;
    private Long salaryFrom;
    private Long salaryTo;
    private String source;
}