package co.com.bancolombia.consumer;

import co.com.bancolombia.consumer.dto.ValidateTokenDTO;
import co.com.bancolombia.model.auth.User;
import co.com.bancolombia.model.auth.gateway.IValidateToken;
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
public class AuthConsumer implements IValidateToken {

    private final WebClient client;
    private static final String SERVICE_NAME = "user-service";
    private static final String PATH_VALIDATE_TOKEN = "/api/v1/token";

    @Override
    @CircuitBreaker(name = "validateToken", fallbackMethod = "validateTokenFallback")
    public Mono<User> validateToken(String token) {
        return client
                .post()
                .uri(PATH_VALIDATE_TOKEN)
                .bodyValue(new ValidateTokenDTO(token))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new ExternalServiceCommunicationException(
                                        SERVICE_NAME, PATH_VALIDATE_TOKEN,
                                        "Error 4xx al validar token: " + token +
                                                " (status=" + resp.statusCode().value() + ", body=" + body + ")",
                                        null)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new ExternalServiceCommunicationException(
                                        SERVICE_NAME, PATH_VALIDATE_TOKEN,
                                        "Error 5xx comunicando con " + SERVICE_NAME +
                                                " al validar token: " + token +
                                                " (status=" + resp.statusCode().value() + ", body=" + body + ")",
                                        null)))
                )
                .bodyToMono(User.class);

    }

    private Mono<Boolean> validateTokenFallback(String token, Throwable cause) {
        log.error("Fallback activado para validar token {}. Causa: {}", token, cause.toString());
        return Mono.error(new ExternalServiceCommunicationException(
                SERVICE_NAME,
                PATH_VALIDATE_TOKEN,
                "Communication problems with the external service, it is not possible to process the user validation request.",
                cause
        ));
    }
}
