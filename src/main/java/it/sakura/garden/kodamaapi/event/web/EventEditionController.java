package it.sakura.garden.kodamaapi.event.web;

import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.event.dto.EventEditionRequest;
import it.sakura.garden.kodamaapi.event.dto.EventEditionResponse;
import it.sakura.garden.kodamaapi.event.service.EventEditionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;

/**
 * API CRUD sulle edizioni evento. La lista accetta {@code from} e {@code to}
 * (ISO-8601, es. {@code 2026-11-20T00:00:00Z}) per filtrare per data
 * pianificata nell'intervallo {@code [from, to)}: è il percorso usato dal cron
 * del bot per le notizie del giorno.
 */
@RestController
@RequestMapping("/api/v1/event-editions")
public class EventEditionController {

    private final EventEditionService eventEditionService;

    public EventEditionController(EventEditionService eventEditionService) {
        this.eventEditionService = eventEditionService;
    }

    @GetMapping
    public PageResponse<EventEditionResponse> list(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(size = 20, sort = "plannedAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return eventEditionService.findAll(from, to, pageable);
    }

    @GetMapping("/{id}")
    public EventEditionResponse getById(@PathVariable Integer id) {
        return eventEditionService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public ResponseEntity<EventEditionResponse> create(@Valid @RequestBody EventEditionRequest request,
                                                       UriComponentsBuilder uriBuilder) {
        EventEditionResponse created = eventEditionService.create(request);

        return ResponseEntity
                .created(uriBuilder.path("/api/v1/event-editions/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public EventEditionResponse update(@PathVariable Integer id,
                                       @Valid @RequestBody EventEditionRequest request) {
        return eventEditionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventEditionService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
