CREATE TABLE match_results (
    id             BIGSERIAL PRIMARY KEY,
    vacancy_id     BIGINT    NOT NULL,
    resume_id      BIGINT    NOT NULL,
    score          INT       NOT NULL,

    matched_skills TEXT[],
    missing_skills TEXT[],
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_match_vacancy
        FOREIGN KEY (vacancy_id) REFERENCES vacancies(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_match_resume
        FOREIGN KEY (resume_id) REFERENCES resumes(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_match UNIQUE (vacancy_id, resume_id)
);