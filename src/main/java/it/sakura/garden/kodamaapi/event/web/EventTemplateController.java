package it.sakura.garden.kodamaapi.event.web;

import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateImage;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateRequest;
import it.sakura.garden.kodamaapi.event.dto.EventTemplateResponse;
import it.sakura.garden.kodamaapi.event.service.EventTemplateService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * API CRUD sui template evento. L'immagine si invia come data URI nel corpo
 * JSON e si rilegge in binario da {@code /{id}/image}.
 */
@RestController
@RequestMapping("/api/v1/event-templates")
public class EventTemplateController {

    private final EventTemplateService eventTemplateService;

    public EventTemplateController(EventTemplateService eventTemplateService) {
        this.eventTemplateService = eventTemplateService;
    }

    @GetMapping
    public PageResponse<EventTemplateResponse> list(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return eventTemplateService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public EventTemplateResponse getById(@PathVariable Integer id) {
        return eventTemplateService.findById(id);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Integer id) {
        EventTemplateImage image = eventTemplateService.findImage(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .body(image.data());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public ResponseEntity<EventTemplateResponse> create(@Valid @RequestBody EventTemplateRequest request,
                                                        UriComponentsBuilder uriBuilder) {
        EventTemplateResponse created = eventTemplateService.create(request);

        return ResponseEntity
                .created(uriBuilder.path("/api/v1/event-templates/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public EventTemplateResponse update(@PathVariable Integer id,
                                        @Valid @RequestBody EventTemplateRequest request) {
        return eventTemplateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventTemplateService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
