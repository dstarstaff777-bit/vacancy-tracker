package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.VacancyDto;
import org.example.entity.VacancyStatus;
import org.example.repository.VacancyRepository;
import org.example.entity.Vacancy;
import org.example.exception.VacancyNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
// @Service говорит Spring что это бизнес-логика
// Spring создаст синглтон этого класса при старте
// и будет инжектить его везде где нужно

@RequiredArgsConstructor
// Lombok генерирует конструктор для всех final полей
// Spring будет использовать этот конструктор для DI
// это современный способ вместо @Autowired

@Slf4j
// Lombok создаёт поле log для логирования
// можно использовать log.info(), log.error() и тд
// без явного создания Logger
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    // final означает что поле инициализируется один раз
    // в конструкторе и больше не меняется
    // это гарантия immutability для зависимостей

    /**
     * Получить вакансию по ID
     * Если не найдена — выбросить исключение
     */
    @Transactional(readOnly = true)
    // readOnly = true оптимизация для чтения
    // говорит Hibernate что мы не будем изменять данные
    // Hibernate может пропустить проверку dirty checking
    // и другие overhead связанные с записью
    public VacancyDto getById(Long id) {
        log.debug("Getting vacancy by id: {}", id);
        // log.debug для детальной отладки
        // на проде эти логи обычно выключены

        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException(
                        "Vacancy not found with id: " + id
                ));
        // findById возвращает Optional<Vacancy>
        // orElseThrow — если Optional пустой, бросаем исключение
        // иначе достаём значение

        return mapToDto(vacancy);
    }

    /**
     * Получить список вакансий с фильтрацией
     */
    @Transactional(readOnly = true)
    public List<VacancyDto> getVacancies(
            VacancyStatus status,
            int page,
            int size
    ) {
        log.debug("Getting vacancies with status: {}, page: {}, size: {}",
                status, page, size);

        List<Vacancy> vacancies;

        if (status != null) {
            // PageRequest.of(page, size) создаёт объект пагинации
            // page начинается с 0 — первая страница это 0, не 1
            vacancies = vacancyRepository.findByStatusOrderByParsedAtDesc(
                    status,
                    PageRequest.of(page, size)
            );
        } else {
            // если статус не указан — берём все
            // но с пагинацией чтобы не грузить всю БД
            vacancies = vacancyRepository.findAll(
                    PageRequest.of(page, size)
            ).getContent();
            // findAll с Pageable возвращает Page<Vacancy>
            // getContent() достаёт список из Page
        }

        return vacancies.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        // stream API
    }
    @Transactional
    // @Transactional без readOnly = true означает что
    // это транзакция на запись. Если метод упадёт с exception
    // все изменения в БД откатятся автоматически
    public VacancyDto save(Vacancy vacancy) {
        log.debug("Saving vacancy: {}", vacancy.getTitle());

        // save() в JPA работает как upsert:
        // если id == null → INSERT
        // если id != null → UPDATE
        Vacancy saved = vacancyRepository.save(vacancy);

        log.info("Vacancy saved with id: {}", saved.getId());
        return mapToDto(saved);
    }

    /**
     * Обновить статус вакансии
     * Используется для изменения NEW → PROCESSING → PROCESSED
     */
    @Transactional
    public void updateStatus(Long id, VacancyStatus newStatus) {
        log.debug("Updating vacancy {} status to {}", id, newStatus);

        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException(
                        "Vacancy not found with id: " + id
                ));

        vacancy.setStatus(newStatus);
        // Не нужно вызывать save()!
        // Hibernate отслеживает изменения в managed entity
        // внутри транзакции и автоматически сохранит при commit
        // это называется "dirty checking"

        log.info("Vacancy {} status updated to {}", id, newStatus);
    }

    /**
     * Найти вакансии которые зависли в обработке
     * Например если Kafka consumer упал и вакансия
     * больше 2 часов в статусе PROCESSING
     */
    @Transactional(readOnly = true)
    public List<VacancyDto> findStuckVacancies(int hoursThreshold) {
        log.debug("Finding stuck vacancies older than {} hours", hoursThreshold);

        LocalDateTime before = LocalDateTime.now()
                .minusHours(hoursThreshold);

        List<Vacancy> stuck = vacancyRepository.findStuckVacancies(
                VacancyStatus.PROCESSING,
                before
        );

        log.info("Found {} stuck vacancies", stuck.size());

        return stuck.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Маппинг Entity → DTO
     * Entity не отдаём напрямую клиенту потому что:
     * 1. Entity может иметь связи (@OneToMany) которые вызовут N+1 запросы
     * 2. Не хотим раскрывать внутреннюю структуру БД
     * 3. DTO можем кастомизировать под нужды API
     */
    private VacancyDto mapToDto(Vacancy vacancy) {
        return VacancyDto.builder()
                .id(vacancy.getId())
                .title(vacancy.getTitle())
                .company(vacancy.getCompany())
                .skills(vacancy.getSkills())
                .salaryFrom(vacancy.getSalaryFrom())
                .salaryTo(vacancy.getSalaryTo())
                .experience(vacancy.getExperience())
                .source(vacancy.getSource())
                .sourceUrl(vacancy.getSourceUrl())
                .status(vacancy.getStatus())
                .parsedAt(vacancy.getParsedAt())
                .build();
        // можно использовать MapStruct для автоматического маппинга
        // но для pet-проекта ручной маппинг тоже ок
    }
}
