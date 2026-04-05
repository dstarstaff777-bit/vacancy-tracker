package org.example.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai.AiService;
import org.example.ai.dto.VacancyAiResponse;
import org.example.dto.VacancyMessage;
import org.example.entity.VacancyStatus;
import org.example.repository.VacancyRepository;
import org.example.entity.Vacancy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyConsumer {

    private final VacancyRepository vacancyRepository;
    private final AiService aiService;
    private final VacancyProducer vacancyProducer;

    @KafkaListener(
            topics = "${kafka.topics.vacancy-raw}",

            groupId = "${spring.kafka.consumer.group-id}",

            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeRawVacancy(
            VacancyMessage message,
            Acknowledgment acknowledgment

    ) {
        log.info("Received raw vacancy: {}", message.getSourceUrl());

        try {
            if (vacancyRepository.existsBySourceUrl(message.getSourceUrl())) {
                log.warn("Vacancy already exists: {}", message.getSourceUrl());
                acknowledgment.acknowledge();
                return;
            }

            Vacancy vacancy = Vacancy.builder()
                    .title(message.getTitle())
                    .company(message.getCompany())
                    .sourceUrl(message.getSourceUrl())
                    .source(message.getSource())
                    .salaryFrom(message.getSalaryFrom())
                    .salaryTo(message.getSalaryTo())
                    .status(VacancyStatus.NEW)
                    .build();

            vacancy = vacancyRepository.save(vacancy);
            log.info("Vacancy saved with id: {}", vacancy.getId());

            vacancy.setStatus(VacancyStatus.PROCESSING);
            vacancyRepository.save(vacancy);

            VacancyAiResponse aiResponse = aiService.extractSkillsFromVacancy(
                    vacancy.getTitle() + " " +
                            (vacancy.getCompany() != null ? vacancy.getCompany() : "")
            );

            vacancy.setSkills(aiResponse.getSkills());
            vacancy.setExperience(aiResponse.getExperience());
            vacancy.setStatus(VacancyStatus.PROCESSED);
            vacancyRepository.save(vacancy);

            log.info("Vacancy processed successfully: {}", vacancy.getId());

            VacancyMessage processedMessage = VacancyMessage.builder()
                    .vacancyId(vacancy.getId())
                    .title(vacancy.getTitle())
                    .company(vacancy.getCompany())
                    .sourceUrl(vacancy.getSourceUrl())
                    .source(vacancy.getSource())
                    .build();

            vacancyProducer.sendProcessedVacancy(processedMessage);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing vacancy: {}", message.getSourceUrl(), e);

            vacancyRepository.findBySourceUrl(message.getSourceUrl())
                    .ifPresent(v -> {
                        v.setStatus(VacancyStatus.FAILED);
                        vacancyRepository.save(v);
                    });

            vacancyProducer.sendFailedVacancy(message, e.getMessage());

            acknowledgment.acknowledge();
        }
    }

}