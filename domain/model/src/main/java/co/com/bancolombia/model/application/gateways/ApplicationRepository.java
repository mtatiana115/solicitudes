package co.com.bancolombia.model.application.gateways;

import co.com.bancolombia.model.application.Application;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ApplicationRepository {
    Mono<Application> save (Application application);

    Mono<Application> findById(String id);

    Flux<Application> filterApplications(int offset, int limit, Integer loanTypeId, Integer statusId);

    Mono<Long> countFilteredApplications(Integer loanTypeId, Integer statusId);

    Mono<Application> findByEmailAndId(String email, String id);
}
