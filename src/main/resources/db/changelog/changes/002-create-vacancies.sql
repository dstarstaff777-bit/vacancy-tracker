CREATE TABLE vacancies (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(500) NOT NULL,
    company     VARCHAR(255),
    raw_text    TEXT,
    skills      TEXT[],
    salary_from BIGINT,
    salary_to   BIGINT,
    experience  VARCHAR(50),
    source      VARCHAR(100) NOT NULL,
    source_url  VARCHAR(1000) NOT NULL UNIQUE,
    status      VARCHAR(50)  NOT NULL DEFAULT 'NEW',
    parsed_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vacancies_status ON vacancies(status);