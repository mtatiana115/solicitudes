package co.com.bancolombia.model.application.gateways;

import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.application.dto.ApplicationFilterIds;
import co.com.bancolombia.model.application.dto.ApplicationList;
import co.com.bancolombia.model.application.Application;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ApplicationRepository {
    Mono<Application> save (Application application);

    Flux<ApplicationList> filterApplications(int page, int size, ApplicationFilterIds filter);
}
