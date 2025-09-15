package co.com.bancolombia.r2dbc.reactiverepositorystatus;

import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.status.Status;
import co.com.bancolombia.model.status.gateways.StatusRepository;
import co.com.bancolombia.r2dbc.entities.LoanTypeEntity;
import co.com.bancolombia.r2dbc.entities.StatusEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class StatusReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Status,
        StatusEntity,
        Integer,
        StatusReactiveRepository
        > implements StatusRepository {

    public StatusReactiveRepositoryAdapter(StatusReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, StatusEntity -> mapper.map(StatusEntity, Status.class));
    }

    @Override
    public Mono<Integer> findIdByName(String name) {
        return repository.findIdByName(name);
    }

    @Override
    public Mono<Status> findById (Integer id){
        return repository.findById(id)
                .map(entity -> mapper.map(entity, Status.class));
    }
}