package it.sakura.garden.kodamaapi.config;

import it.sakura.garden.kodamaapi.common.web.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Restituisce {@link ApiError} anche per gli errori di sicurezza.
 *
 * <p>Senza questo componente 401 e 403 verrebbero servite dalla pagina di
 * errore di default in HTML: il bot Discord riceverebbe un corpo che non sa
 * interpretare proprio nei casi in cui deve capire se rinnovare il token.
 *
 * <p>Le due responsabilità stanno nella stessa classe perché producono lo
 * stesso formato e differiscono solo per status e messaggio.
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final String BEARER_CHALLENGE = "Bearer realm=\"kodama-api\"";

    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Token assente, scaduto o non verificabile. */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {

        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, BEARER_CHALLENGE);
        write(request, response, HttpStatus.UNAUTHORIZED,
                "Autenticazione richiesta: fornire un access token valido nell'header Authorization");
    }

    /** Token valido ma privo dei ruoli necessari all'operazione. */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        write(request, response, HttpStatus.FORBIDDEN,
                "Permessi insufficienti per questa operazione");
    }

    private void write(HttpServletRequest request, HttpServletResponse response,
                       HttpStatus status, String message) throws IOException {

        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
