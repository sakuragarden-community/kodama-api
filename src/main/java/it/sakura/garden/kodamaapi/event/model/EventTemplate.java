package it.sakura.garden.kodamaapi.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Contenuti con cui un {@link Event} viene pubblicato su Discord: l'evento
 * programmato, la notizia nel canale notizie e il messaggio di apertura del
 * thread dei commenti.
 */
@Entity
@Table(name = "event_templates")
public class EventTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Nome con cui il template viene riconosciuto nelle liste, es. nelle select del bot. */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "discord_event_title", nullable = false, length = 100)
    private String discordEventTitle;

    @Column(name = "discord_event_description", nullable = false, columnDefinition = "text")
    private String discordEventDescription;

    /**
     * Immagine come data URI base64 ({@code data:image/png;base64,...}): gli
     * URL degli allegati Discord scadono, il contenuto no.
     */
    @Column(name = "discord_event_image", nullable = false, columnDefinition = "text")
    private String discordEventImage;

    /** ID del canale notizie Discord in cui pubblicare la notizia. */
    @Column(name = "discord_news_channel", nullable = false, length = 32)
    private String discordNewsChannel;

    @Column(name = "discord_news_content", nullable = false, columnDefinition = "text")
    private String discordNewsContent;

    /** Messaggio di apertura del thread; {@code null} per l'invito generico a commentare. */
    @Column(name = "discord_thread_post", columnDefinition = "text")
    private String discordThreadPost;

    protected EventTemplate() {
        // richiesto da JPA
    }

    public EventTemplate(Event event, String name, String discordEventTitle, String discordEventDescription,
                         String discordEventImage, String discordNewsChannel, String discordNewsContent,
                         String discordThreadPost) {
        this.event = event;
        this.name = name;
        this.discordEventTitle = discordEventTitle;
        this.discordEventDescription = discordEventDescription;
        this.discordEventImage = discordEventImage;
        this.discordNewsChannel = discordNewsChannel;
        this.discordNewsContent = discordNewsContent;
        this.discordThreadPost = discordThreadPost;
    }

    public Integer getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiscordEventTitle() {
        return discordEventTitle;
    }

    public void setDiscordEventTitle(String discordEventTitle) {
        this.discordEventTitle = discordEventTitle;
    }

    public String getDiscordEventDescription() {
        return discordEventDescription;
    }

    public void setDiscordEventDescription(String discordEventDescription) {
        this.discordEventDescription = discordEventDescription;
    }

    public String getDiscordEventImage() {
        return discordEventImage;
    }

    public void setDiscordEventImage(String discordEventImage) {
        this.discordEventImage = discordEventImage;
    }

    public String getDiscordNewsChannel() {
        return discordNewsChannel;
    }

    public void setDiscordNewsChannel(String discordNewsChannel) {
        this.discordNewsChannel = discordNewsChannel;
    }

    public String getDiscordNewsContent() {
        return discordNewsContent;
    }

    public void setDiscordNewsContent(String discordNewsContent) {
        this.discordNewsContent = discordNewsContent;
    }

    public String getDiscordThreadPost() {
        return discordThreadPost;
    }

    public void setDiscordThreadPost(String discordThreadPost) {
        this.discordThreadPost = discordThreadPost;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof EventTemplate template && id != null && id.equals(template.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "EventTemplate{id=%d, name='%s'}".formatted(id, name);
    }
}
