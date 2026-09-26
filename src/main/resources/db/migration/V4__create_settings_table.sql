-- Entità "Setting": configurazione applicativa chiave/valore, modificabile a
-- runtime senza ridistribuire l'applicazione. Il "path" è la chiave naturale
-- (es. "discord.channels.welcome"), "type" suggerisce come interpretare il
-- valore, che resta sempre memorizzato come testo.
CREATE TABLE settings
(
    id    SERIAL PRIMARY KEY,
    path  VARCHAR(255) NOT NULL,
    type  VARCHAR(32)  NOT NULL DEFAULT 'text',
    value TEXT,

    CONSTRAINT uq_settings_path UNIQUE (path)
);
