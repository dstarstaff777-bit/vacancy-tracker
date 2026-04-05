package org.example.repository;
import org.example.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    List<MatchResult> findByResumeIdOrderByScoreDesc(Long resumeId);

    @Query("""
    SELECT m FROM MatchResult m 
    WHERE m.resume.id = :resumeId 
    AND m.score >= :minScore 
    ORDER BY m.score DESC
""")
    List<MatchResult> findByResume_IdAndScoreGreaterThanEqual(
            @Param("resumeId") Long resumeId,
            @Param("minScore") int minScore
    );

    Optional<MatchResult> findByVacancyIdAndResumeId(
            Long vacancyId,
            Long resumeId
    );

    @Modifying
    @Query("DELETE FROM MatchResult m WHERE m.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Long resumeId);
}