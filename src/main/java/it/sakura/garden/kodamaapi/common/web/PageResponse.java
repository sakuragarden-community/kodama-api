package it.sakura.garden.kodamaapi.common.web;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Involucro di paginazione esposto dalle API.
 *
 * <p>Esiste per non far trapelare la serializzazione di {@code Page} di Spring
 * Data, che non è un contratto stabile: il bot Discord dipende da questo record
 * e non dai dettagli interni della libreria.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last) {

    /** Converte una {@link Page} di entità in una pagina di DTO. */
    public static <E, T> PageResponse<T> from(Page<E> source, Function<E, T> mapper) {
        return new PageResponse<>(
                source.getContent().stream().map(mapper).toList(),
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.isLast());
    }
}
