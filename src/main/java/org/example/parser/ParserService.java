package org.example.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.messaging.VacancyProducer;
import org.example.repository.VacancyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParserService {

    private final HhParser hhParser;
    private final VacancyRepository vacancyRepository;
    private final VacancyProducer vacancyProducer;

    private static final List<String> HH_URLS = List.of(
            "https://hh.ru/search/vacancy?text=Java+Middle&area=1",
            "https://hh.ru/search/vacancy?text=Java+Developer&area=1"
    );

    public void parseAndSendToKafka() {
        log.info("Начинаем парсинг вакансий");

        int totalParsed = 0;
        int totalSent = 0;

        for (String url : HH_URLS) {
            List<ParsedVacancy> vacancies = hhParser.parseSearchPage(url);
            totalParsed += vacancies.size();

            for (ParsedVacancy parsedVacancy : vacancies) {
                if (!vacancyRepository.existsBySourceUrl(parsedVacancy.getSourceUrl())) {
                    vacancyProducer.sendRawVacancy(parsedVacancy);
                    totalSent++;
                } else {
                    log.debug("Вакансия уже существует: {}",
                            parsedVacancy.getSourceUrl());
                }
            }
        }

        log.info("Парсинг завершён. Найдено: {}, отправлено в Kafka: {}",
                totalParsed, totalSent);
    }
}