package co.com.bancolombia.model.application.gateways;

import co.com.bancolombia.model.application.Application;
import reactor.core.publisher.Mono;

public interface ApplicationRepository {
    Mono<Application> save (Application application);
}
