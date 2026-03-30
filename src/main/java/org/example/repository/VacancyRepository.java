package org.example.vacancy;

import org.example.entity.VacancyStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
// @Repository — Spring создаст реализацию этого
// интерфейса автоматически, не нужно писать
// никакого SQL для базовых операций
public interface VacancyRepository
        extends JpaRepository<Vacancy, Long> {
    // JpaRepository<Vacancy, Long> означает:
    // сущность — Vacancy, тип первичного ключа — Long
    // уже есть методы: save(), findById(), findAll(),
    // deleteById() и много других

    // Spring Data сам напишет запрос по имени метода:
    // SELECT * FROM vacancies WHERE status = ?
    List<Vacancy> findByStatus(VacancyStatus status);

    // SELECT * FROM vacancies WHERE status = ?
    // ORDER BY parsed_at DESC LIMIT ?
    List<Vacancy> findByStatusOrderByParsedAtDesc(
            VacancyStatus status,
            Pageable pageable
    );

    // проверяем есть ли уже такая вакансия
    // SELECT COUNT(*) > 0 FROM vacancies WHERE source_url = ?
    boolean existsBySourceUrl(String sourceUrl);

    // кастомный запрос через @Query когда имя метода
    // стало бы слишком длинным или запрос сложный
    @Query("SELECT v FROM Vacancy v WHERE v.status = :status " +
            "AND v.parsedAt < :before")
    List<Vacancy> findStuckVacancies(
            @Param("status") VacancyStatus status,
            @Param("before") LocalDateTime before
    );
}