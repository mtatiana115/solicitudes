package co.com.bancolombia.model.auth.gateway;

import co.com.bancolombia.model.auth.User;
import reactor.core.publisher.Mono;

public interface IValidateToken {

    Mono<User> validateToken (String token);
}
