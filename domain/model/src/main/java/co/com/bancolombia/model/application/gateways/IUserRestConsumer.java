package co.com.bancolombia.model.application.gateways;

import co.com.bancolombia.model.auth.User;
import reactor.core.publisher.Mono;

public interface IUserRestConsumer {
    Mono<Boolean> existsUserByEmail(String email);

    Mono<User> findUserByEmail(String email, String token);
}
