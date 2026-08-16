-- Entità "Section": rappresenta una sezione tematica del server Discord.
CREATE TABLE sections
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50)  NOT NULL,

    CONSTRAINT uq_sections_code UNIQUE (code)
);
