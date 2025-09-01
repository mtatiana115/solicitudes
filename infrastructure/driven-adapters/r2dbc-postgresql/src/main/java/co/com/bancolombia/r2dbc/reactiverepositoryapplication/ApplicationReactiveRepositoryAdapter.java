package co.com.bancolombia.r2dbc.reactiverepositoryapplication;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.r2dbc.entities.ApplicationEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Application,
        ApplicationEntity,
        String,
        ApplicationReactiveRepository
        > implements ApplicationRepository {
    public ApplicationReactiveRepositoryAdapter(ApplicationReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, ApplicationEntity -> mapper.map(ApplicationEntity, Application.class));

    }
}