package it.sakura.garden.kodamaapi.event.web;

import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.event.dto.EventRequest;
import it.sakura.garden.kodamaapi.event.dto.EventResponse;
import it.sakura.garden.kodamaapi.event.service.EventService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/** API CRUD sugli eventi, con la stessa separazione di ruoli delle sezioni. */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public PageResponse<EventResponse> list(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return eventService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable Integer id) {
        return eventService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request,
                                                UriComponentsBuilder uriBuilder) {
        EventResponse created = eventService.create(request);

        return ResponseEntity
                .created(uriBuilder.path("/api/v1/events/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public EventResponse update(@PathVariable Integer id, @Valid @RequestBody EventRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
