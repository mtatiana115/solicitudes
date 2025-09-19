package co.com.bancolombia.r2dbc.reactiverepositoryapplication;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.r2dbc.entities.ApplicationEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class ApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Application,
        ApplicationEntity,
        String,
        ApplicationReactiveRepository
        > implements ApplicationRepository {

    private final R2dbcEntityTemplate template;
    private final ObjectMapper mapper;

    public ApplicationReactiveRepositoryAdapter(ApplicationReactiveRepository repository, ObjectMapper mapper, R2dbcEntityTemplate template) {
        super(repository, mapper, ApplicationEntity -> mapper.map(ApplicationEntity, Application.class));
        this.template = template;
        this.mapper = mapper;

    }

    @Override
    public Flux<Application> filterApplications(int offset, int limit, Integer loanTypeId, Integer statusId) {
        log.info("Fetching applications with filters: loanTypeId={}, statusId={}, offset={}, limit={}",
                loanTypeId, statusId, offset, limit);

        return repository
                .findFilteredApplications(offset, limit, loanTypeId, statusId)
                .map(this::toEntity);
    }

    @Override
    public Mono<Long> countFilteredApplications(Integer loanTypeId, Integer statusId) {
        log.info("Counting applications with filters: loanTypeId={}, statusId={}", loanTypeId, statusId);

        return repository
                .countFilteredApplications(loanTypeId, statusId)
                .doOnSuccess(count -> log.info("Found {} applications with these filters.", count));
    }

    @Override
    public Mono<Application> findByEmailAndId(String email, String id) {
        return repository.findByEmailAndId(email, id)
                .map(this::toEntity);
    }
}