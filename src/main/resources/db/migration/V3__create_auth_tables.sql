-- Entità "User": credenziali dei client macchina (es. il bot Discord) che
-- consumano le API. Volutamente separata da "members", che modella invece le
-- persone della community e non ha alcun ruolo nell'autenticazione.
CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    client_id     VARCHAR(100) NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    display_name  VARCHAR(150),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ,

    CONSTRAINT uq_users_client_id UNIQUE (client_id)
);

CREATE TABLE user_roles
(
    user_id BIGINT      NOT NULL,
    role    VARCHAR(32) NOT NULL,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
