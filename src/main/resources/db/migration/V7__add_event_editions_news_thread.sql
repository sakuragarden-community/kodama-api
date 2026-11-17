-- Campo "discord_news_thread_id" di EventEdition: thread della notizia della
-- settimana prima, salvato dal bot alla pubblicazione. Il giorno dell'evento
-- il promemoria viene pubblicato in questo thread, non nel canale notizie.
ALTER TABLE event_editions
    ADD COLUMN discord_news_thread_id VARCHAR(32);
