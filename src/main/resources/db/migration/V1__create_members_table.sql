-- Entità "Member": rappresenta un membro della community Kodama Nest.
CREATE TABLE members
(
    id           SERIAL PRIMARY KEY,
    discord_id   VARCHAR(32)  NOT NULL,
    username     VARCHAR(100) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    joined_at    TIMESTAMPTZ,
    left_at      TIMESTAMPTZ,
    status       VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    presentation TEXT,
    experience   INTEGER      NOT NULL DEFAULT 0,

    CONSTRAINT uq_members_discord_id UNIQUE (discord_id),
    CONSTRAINT ck_members_experience_non_negative CHECK (experience >= 0)
);

CREATE INDEX idx_members_status ON members (status);
