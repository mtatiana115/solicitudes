package co.com.bancolombia.model.status.gateways;

import co.com.bancolombia.model.status.Status;
import reactor.core.publisher.Mono;

public interface StatusRepository {
    Mono<Status> findById(Integer id);
    Mono<Integer> findIdByName(String name);
}
