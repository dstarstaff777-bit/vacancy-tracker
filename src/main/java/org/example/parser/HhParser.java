package org.example.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HhParser {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/120.0.0.0 Safari/537.36";

    public List<ParsedVacancy> parseSearchPage(String url) {
        List<ParsedVacancy> vacancies = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000) // 10 секунд таймаут
                    .get();

            Elements vacancyCards = doc.select("div.vacancy-serp-item");

            log.info("Найдено {} вакансий на странице {}",
                    vacancyCards.size(), url);

            for (Element card : vacancyCards) {
                try {
                    ParsedVacancy vacancy = parseVacancyCard(card);
                    vacancies.add(vacancy);
                } catch (Exception e) {
                    log.error("Ошибка парсинга карточки вакансии", e);
                }
            }

        } catch (IOException e) {
            log.error("Ошибка загрузки страницы: {}", url, e);

        }

        return vacancies;
    }

    private ParsedVacancy parseVacancyCard(Element card) {
        Element titleElement = card.selectFirst("a.bloko-link");
        String title = titleElement != null
                ? titleElement.text()
                : "Не указано";
        String vacancyUrl = titleElement != null
                ? titleElement.attr("href")
                : "";

        Element companyElement = card.selectFirst("a.bloko-link[data-qa=vacancy-serp__vacancy-employer]");
        String company = companyElement != null
                ? companyElement.text()
                : "Не указано";

        Element salaryElement = card.selectFirst("span[data-qa=vacancy-serp__vacancy-compensation]");
        String salaryText = salaryElement != null
                ? salaryElement.text()
                : null;

        SalaryRange salary = parseSalary(salaryText);

        return ParsedVacancy.builder()
                .title(title)
                .company(company)
                .sourceUrl(vacancyUrl)
                .salaryFrom(salary.from())
                .salaryTo(salary.to())
                .source("hh.ru")
                .build();
    }

    private SalaryRange parseSalary(String salaryText) {
        if (salaryText == null || salaryText.isBlank()) {
            return new SalaryRange(null, null);
        }

        Long from = null;
        Long to = null;

        try {

            String cleaned = salaryText
                    .replace("₽", "")
                    .replace(" ", "")
                    .toLowerCase();

            if (cleaned.contains("от") && cleaned.contains("до")) {
                String[] parts = cleaned.split("до");
                from = Long.parseLong(parts[0].replace("от", ""));
                to = Long.parseLong(parts[1]);
            } else if (cleaned.contains("от")) {
                from = Long.parseLong(cleaned.replace("от", ""));
            } else if (cleaned.contains("до")) {
                to = Long.parseLong(cleaned.replace("до", ""));
            }
        } catch (NumberFormatException e) {
            log.warn("Не удалось распарсить зарплату: {}", salaryText);
        }

        return new SalaryRange(from, to);
    }

    private record SalaryRange(Long from, Long to) {}
}