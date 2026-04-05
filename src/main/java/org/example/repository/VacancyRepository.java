package org.example.repository;

import org.example.entity.VacancyStatus;
import org.example.entity.Vacancy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VacancyRepository
        extends JpaRepository<Vacancy, Long> {

    List<Vacancy> findByStatus(VacancyStatus status);

    List<Vacancy> findByStatusOrderByParsedAtDesc(
            VacancyStatus status,
            Pageable pageable
    );

    boolean existsBySourceUrl(String sourceUrl);

    @Query("SELECT v FROM Vacancy v WHERE v.status = :status " +
            "AND v.parsedAt < :before")
    List<Vacancy> findStuckVacancies(
            @Param("status") VacancyStatus status,
            @Param("before") LocalDateTime before
    );
    Optional<Vacancy> findBySourceUrl(String sourceUrl);
}