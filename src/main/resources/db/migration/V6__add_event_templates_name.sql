-- Campo "name" di EventTemplate: nome con cui lo Staff riconosce il template
-- nelle select del bot. I template già esistenti ricevono come nome il titolo
-- dell'evento Discord, poi la colonna diventa obbligatoria.
-- Postgres non permette di inserire una colonna in una posizione precisa: la
-- colonna finisce in coda alla tabella, l'ordine logico (dopo "event_id") è
-- quello dell'entità.
ALTER TABLE event_templates
    ADD COLUMN name VARCHAR(100);

UPDATE event_templates
SET name = discord_event_title;

ALTER TABLE event_templates
    ALTER COLUMN name SET NOT NULL;
