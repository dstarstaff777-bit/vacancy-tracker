package org.example.vacancy.exception;

public class VacancyNotFoundException extends RuntimeException {
    // RuntimeException (unchecked) — не нужно объявлять в throws
    // для бизнес-исключений это правильный выбор

    public VacancyNotFoundException(String message) {
        super(message);
    }
}