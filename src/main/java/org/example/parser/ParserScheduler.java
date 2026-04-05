package org.example.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParserScheduler {

    private final ParserService parserService;

    @Scheduled(cron = "0 0 */6 * * *")
    public void scheduledParsing() {
        log.info("Запуск планового парсинга вакансий");
        try {
            parserService.parseAndSendToKafka();
        } catch (Exception e) {
            log.error("Ошибка при плановом парсинге", e);

        }
    }
}