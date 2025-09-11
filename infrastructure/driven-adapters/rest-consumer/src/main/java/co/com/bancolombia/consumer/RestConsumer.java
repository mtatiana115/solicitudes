package co.com.bancolombia.consumer;

import co.com.bancolombia.consumer.dto.ExistsUserResponse;
import co.com.bancolombia.consumer.dto.UserDTO;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.auth.User;
import co.com.bancolombia.model.exceptions.ExternalServiceCommunicationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements IUserRestConsumer {
    private final WebClient client;
    private static final String SERVICE_NAME = "user-service";
    private static final String PATH_VALIDATE_USER_BY_EMAIL = "/api/v1/users/email/{email}/exists";
    private static final String PATH_FIND_USER_BY_EMAIL = "/api/v1/users/email/{email}";

    @Override
    @CircuitBreaker(name = "existsUserByEmail", fallbackMethod = "validateUserFallback")
    public Mono<Boolean> existsUserByEmail(String email) {
        return client
                .get()
                .uri(PATH_VALIDATE_USER_BY_EMAIL, email)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new ExternalServiceCommunicationException(
                                        SERVICE_NAME, PATH_VALIDATE_USER_BY_EMAIL,
                                        "Error 4xx al validar usuario por email: " + email +
                                                " (status=" + resp.statusCode().value() + ", body=" + body + ")",
                                        null)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new ExternalServiceCommunicationException(
                                        SERVICE_NAME, PATH_VALIDATE_USER_BY_EMAIL,
                                        "Error 5xx comunicando con " + SERVICE_NAME +
                                                " al validar email: " + email +
                                                " (status=" + resp.statusCode().value() + ", body=" + body + ")",
                                        null)))
                )
                .bodyToMono(ExistsUserResponse.class)
                .map(ExistsUserResponse::getExistsUser);
    }

    @Override
    @CircuitBreaker(name = "findUserByEmail", fallbackMethod = "findUserFallback")
    public Mono<User> findUserByEmail(String email) {
        return client
                .get()
                .uri(PATH_FIND_USER_BY_EMAIL, email)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> {
                                    if (resp.statusCode().value() == 404) {
                                        log.info("Usuario no encontrado para email: {}", email);
                                        return Mono.empty(); // Esto hará que se use defaultIfEmpty
                                    }
                                    // Para otros errores 4xx, sí lanza excepción
                                    return Mono.error(new ExternalServiceCommunicationException(
                                            SERVICE_NAME, PATH_FIND_USER_BY_EMAIL,
                                            "Error 4xx al buscar usuario por email: " + email +
                                                    " (status=" + resp.statusCode().value() + ", body=" + body + ")",
                                            null));
                                })
                )
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new ExternalServiceCommunicationException(
                                        SERVICE_NAME, PATH_FIND_USER_BY_EMAIL,
                                        "Error 5xx comunicando con " + SERVICE_NAME +
                                                " al buscar email: " + email +
                                                " (status=" + resp.statusCode().value() + ", body=" + body + ")",
                                        null)))
                )
                .bodyToMono(UserDTO.class)
                .map(this::toUserDomain);
    }

    private Mono<Boolean> validateUserFallback(String email, Throwable cause) {
        log.error("Fallback activado para validar email {}. Causa: {}", email, cause.toString());
        return Mono.error(new ExternalServiceCommunicationException(
                SERVICE_NAME,
                PATH_VALIDATE_USER_BY_EMAIL,
                "Communication problems with the external service, it is not possible to process the user validation request.",
                cause
        ));
    }

    private Mono<User> findUserFallback(String email, Throwable cause) {
        log.error("Fallback activado para buscar usuario con email {}. Causa: {}", email, cause.toString());
        return Mono.empty();
    }

    private User toUserDomain(UserDTO dto) {
        return new User(
                dto.getRolName(),
                dto.getEmail(),
                dto.getDocumentId(),
                dto.getName(),
                dto.getBaseSalary()
        );
    }
}