package it.sakura.garden.kodamaapi.section.web;

import it.sakura.garden.kodamaapi.common.web.PageResponse;
import it.sakura.garden.kodamaapi.section.dto.SectionRequest;
import it.sakura.garden.kodamaapi.section.dto.SectionResponse;
import it.sakura.garden.kodamaapi.section.service.SectionService;
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

/** API CRUD sulle sezioni, con la stessa separazione di ruoli dei membri. */
@RestController
@RequestMapping("/api/v1/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public PageResponse<SectionResponse> list(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return sectionService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public SectionResponse getById(@PathVariable Integer id) {
        return sectionService.findById(id);
    }

    @GetMapping("/by-code/{code}")
    public SectionResponse getByCode(@PathVariable String code) {
        return sectionService.findByCode(code);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public ResponseEntity<SectionResponse> create(@Valid @RequestBody SectionRequest request,
                                                  UriComponentsBuilder uriBuilder) {
        SectionResponse created = sectionService.create(request);

        return ResponseEntity
                .created(uriBuilder.path("/api/v1/sections/{id}").buildAndExpand(created.id()).toUri())
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public SectionResponse update(@PathVariable Integer id, @Valid @RequestBody SectionRequest request) {
        return sectionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BOT', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        sectionService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
