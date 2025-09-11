package co.com.bancolombia.r2dbc.reactiverepositoryapplication;

import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.application.dto.ApplicationFilterIds;
import co.com.bancolombia.model.application.dto.ApplicationList;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.r2dbc.entities.ApplicationEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

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
    public Flux<ApplicationList> filterApplications(int offset, int limit, ApplicationFilterIds idsFilter) {
        return repository.filterApplications(
                offset,
                limit,
                idsFilter.statusId(),
                idsFilter.documentId(),
                idsFilter.loanTypeId()
        );
    }
}