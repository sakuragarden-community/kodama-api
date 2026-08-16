package it.sakura.garden.kodamaapi.auth.service;

import it.sakura.garden.kodamaapi.auth.dto.TokenResponse;
import it.sakura.garden.kodamaapi.auth.exception.OAuth2TokenException;
import it.sakura.garden.kodamaapi.auth.model.ClientCredentials;

/**
 * Emissione degli access token.
 *
 * <p>Astrarre l'operazione dietro un'interfaccia lascia libero il formato del
 * token: oggi è un JWT firmato in RS256, domani potrebbe essere un token opaco
 * senza che il controller ne risenta.
 */
public interface TokenService {

    /**
     * Autentica il client ed emette un access token sul grant
     * {@code client_credentials}.
     *
     * @throws OAuth2TokenException se le credenziali non sono valide o l'utenza è disabilitata
     */
    TokenResponse issueClientCredentialsToken(ClientCredentials credentials);
}
