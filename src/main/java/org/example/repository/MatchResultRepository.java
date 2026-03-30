package org.example.common.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;


import java.util.List;
import java.util.Optional;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    // найти все результаты матчинга для конкретного резюме
    // отсортированные по score от большего к меньшему
    List<MatchResult> findByResumeIdOrderByScoreDesc(Long resumeId);

    // найти результаты матчинга с score выше порога
    // например только те вакансии где совпадение > 70%
    @Query("SELECT m FROM MatchResult m WHERE m.resumeId = :resumeId " +
            "AND m.score >= :minScore ORDER BY m.score DESC")
    List<MatchResult> findByResumeIdAndScoreGreaterThanEqual(
            @Param("resumeId") Long resumeId,
            @Param("minScore") int minScore
    );

    // проверить был ли уже матчинг для этой пары
    // чтобы не пересчитывать одно и то же
    Optional<MatchResult> findByVacancyIdAndResumeId(
            Long vacancyId,
            Long resumeId
    );

    // удалить все результаты матчинга для резюме
    // например когда пользователь обновил резюме
    // и надо пересчитать все матчинги заново
    void deleteByResumeId(Long resumeId);
}