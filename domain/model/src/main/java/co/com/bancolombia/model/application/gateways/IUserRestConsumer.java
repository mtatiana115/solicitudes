package co.com.bancolombia.model.application.gateways;

import reactor.core.publisher.Mono;

public interface IUserRestConsumer {
    Mono<Boolean> existsUserByEmail(String email);
}
