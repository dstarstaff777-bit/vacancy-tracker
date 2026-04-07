package org.example.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.VacancyMessage;
import org.example.parser.ParsedVacancy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyProducer {

    private final KafkaTemplate<String, VacancyMessage> kafkaTemplate;

    @Value("${kafka.topics.vacancy-raw}")
    private String vacancyRawTopic;


    public void sendRawVacancy(ParsedVacancy parsedVacancy) {
        VacancyMessage message = VacancyMessage.builder()
                .title(parsedVacancy.getTitle())
                .company(parsedVacancy.getCompany())
                .sourceUrl(parsedVacancy.getSourceUrl())
                .source(parsedVacancy.getSource())
                .salaryFrom(parsedVacancy.getSalaryFrom())
                .salaryTo(parsedVacancy.getSalaryTo())
                .build();


        String key = parsedVacancy.getSourceUrl();

        CompletableFuture<SendResult<String, VacancyMessage>> future =
                kafkaTemplate.send(vacancyRawTopic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {

                log.error("Failed to send vacancy to Kafka: {}",
                        parsedVacancy.getSourceUrl(), ex);
            } else {

                log.debug("Vacancy sent to Kafka: {} (partition: {}, offset: {})",
                        parsedVacancy.getSourceUrl(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());

            }
        });
    }


    public void sendProcessedVacancy(VacancyMessage message) {
        kafkaTemplate.send("${kafka.topics.vacancy-processed}",
                message.getSourceUrl(), message);

        log.info("Processed vacancy sent to Kafka: {}", message.getSourceUrl());
    }

    public void sendFailedVacancy(VacancyMessage message, String errorReason) {
        log.warn("Sending failed vacancy to DLQ: {} (reason: {})",
                message.getSourceUrl(), errorReason);

        kafkaTemplate.send("${kafka.topics.vacancy-failed}",
                message.getSourceUrl(), message);
    }
}