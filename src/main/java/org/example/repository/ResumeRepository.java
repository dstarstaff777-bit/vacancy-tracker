package org.example.common.repository;

import org.example.common.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // найти все резюме конкретного пользователя
    List<Resume> findByUserId(Long userId);

    // найти конкретное резюме конкретного пользователя
    // нужно для проверки прав доступа — пользователь
    // может редактировать только свои резюме
    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    // проверить существование резюме у пользователя
    boolean existsByIdAndUserId(Long id, Long userId);
}