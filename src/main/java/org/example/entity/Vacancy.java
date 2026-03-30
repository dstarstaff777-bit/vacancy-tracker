package org.example.vacancy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.ExperienceLevel;
import org.example.entity.VacancyStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vacancies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String company;

    @Column(columnDefinition = "TEXT")
    private String rawText;

    // ||===============|| Ошибка Hibernate не может сохранить List<String> в TEXT[]

    @JdbcTypeCode(SqlTypes.ARRAY)
    // @Type из библиотеки hypersistence-utils
    // нужен чтобы Hibernate умел работать
    // с PostgreSQL массивами TEXT[]
    // без этого он не знает как сохранить List<String>
    // в колонку типа TEXT[]
    @Column(columnDefinition = "text[]")
    private List<String> skills;

    private Long salaryFrom;
    private Long salaryTo;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experience;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false, unique = true)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VacancyStatus status = VacancyStatus.NEW;

    @Column(nullable = false, updatable = false)
    private LocalDateTime parsedAt;

    @PrePersist
    protected void onCreate() {
        parsedAt = LocalDateTime.now();
    }
}