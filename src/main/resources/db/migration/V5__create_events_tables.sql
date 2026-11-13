-- Entità "Event": tipologia di evento organizzato dallo Staff, legata a una
-- sezione tematica. "interest" è l'ID del ruolo Discord di riferimento.
CREATE TABLE events
(
    id               SERIAL PRIMARY KEY,
    section_id       INTEGER      NOT NULL,
    name             VARCHAR(100) NOT NULL,
    interest         VARCHAR(32)  NOT NULL,
    max_participants INTEGER      NOT NULL DEFAULT 0,
    is_open          BOOLEAN      NOT NULL,

    CONSTRAINT fk_events_section FOREIGN KEY (section_id) REFERENCES sections (id),
    CONSTRAINT ck_events_max_participants_non_negative CHECK (max_participants >= 0)
);

CREATE INDEX idx_events_section_id ON events (section_id);

-- Entità "EventTemplate": contenuti con cui un evento viene pubblicato su
-- Discord (evento programmato, notizia e thread dei commenti).
-- L'immagine è memorizzata come data URI base64: gli URL degli allegati
-- Discord scadono, quindi non sono un riferimento persistente.
CREATE TABLE event_templates
(
    id                        SERIAL PRIMARY KEY,
    event_id                  INTEGER      NOT NULL,
    discord_event_title       VARCHAR(100) NOT NULL,
    discord_event_description TEXT         NOT NULL,
    discord_event_image       TEXT         NOT NULL,
    discord_news_channel      VARCHAR(32)  NOT NULL,
    discord_news_content      TEXT         NOT NULL,
    discord_thread_post       TEXT,

    CONSTRAINT fk_event_templates_event FOREIGN KEY (event_id) REFERENCES events (id)
);

CREATE INDEX idx_event_templates_event_id ON event_templates (event_id);

-- Entità "EventEdition": singola data in cui un template evento si svolge.
-- "discord_event_id" è l'evento programmato creato dal bot su Discord, usato
-- per linkarlo nelle notizie.
CREATE TABLE event_editions
(
    id                SERIAL PRIMARY KEY,
    event_template_id INTEGER     NOT NULL,
    planned_at        TIMESTAMPTZ NOT NULL,
    discord_event_id  VARCHAR(32),

    CONSTRAINT fk_event_editions_event_template FOREIGN KEY (event_template_id) REFERENCES event_templates (id)
);

CREATE INDEX idx_event_editions_planned_at ON event_editions (planned_at);
