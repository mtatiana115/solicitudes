package co.com.bancolombia.r2dbc.reactiverepositoryapplication;

import co.com.bancolombia.r2dbc.entities.ApplicationEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ApplicationReactiveRepository extends ReactiveCrudRepository<ApplicationEntity, String>, ReactiveQueryByExampleExecutor<ApplicationEntity> {


}
