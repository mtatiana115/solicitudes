package co.com.bancolombia.r2dbc.reactiverepositorystatus;

import co.com.bancolombia.r2dbc.entities.StatusEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StatusReactiveRepository extends ReactiveCrudRepository<StatusEntity, Integer>, ReactiveQueryByExampleExecutor<StatusEntity> {
    @Query("SELECT status_id FROM status WHERE name = :name")
    Mono<Integer> findIdByName(@Param("name") String name);

    @Override
    @Query("SELECT * FROM status WHERE status_id = :status_id")
    Mono<StatusEntity> findById(@Param("status_id") Integer id);

}