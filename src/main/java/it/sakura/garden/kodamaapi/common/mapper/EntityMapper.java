package it.sakura.garden.kodamaapi.common.mapper;

import java.util.Collection;
import java.util.List;

/**
 * Contratto comune dei mapper: isola la conversione DTO ↔ entità in un
 * collaboratore dedicato, così che né i controller né i service debbano
 * conoscere la forma dell'altro layer.
 *
 * @param <E> tipo dell'entità di dominio
 * @param <C> richiesta di creazione
 * @param <U> richiesta di aggiornamento
 * @param <R> DTO di risposta
 */
public interface EntityMapper<E, C, U, R> {

    /** Factory: costruisce una nuova entità a partire dalla richiesta di creazione. */
    E toEntity(C request);

    /** Applica sull'entità gestita i campi valorizzati nella richiesta di aggiornamento. */
    void merge(E entity, U request);

    /** Proietta l'entità sul DTO esposto dalle API. */
    R toResponse(E entity);

    default List<R> toResponseList(Collection<E> entities) {
        return entities.stream().map(this::toResponse).toList();
    }
}
