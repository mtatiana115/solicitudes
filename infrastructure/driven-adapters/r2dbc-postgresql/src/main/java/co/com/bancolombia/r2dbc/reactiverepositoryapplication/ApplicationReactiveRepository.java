package co.com.bancolombia.r2dbc.reactiverepositoryapplication;

import co.com.bancolombia.r2dbc.entities.ApplicationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface ApplicationReactiveRepository extends ReactiveCrudRepository<ApplicationEntity, UUID>, ReactiveQueryByExampleExecutor<ApplicationEntity> {

    @Query("""
        SELECT application_id, amount, term, email, loan_type_id, status_id, document_id
        FROM application
        WHERE (:loanTypeId IS NULL OR loan_type_id = :loanTypeId)
        AND (:statusId IS NULL OR status_id = :statusId)
        ORDER BY application_id
        LIMIT :limit OFFSET :offset
    """)
    Flux<ApplicationEntity> findFilteredApplications(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("loanTypeId") Integer loanTypeId,
            @Param("statusId") Integer statusId
    );

    @Query("""
        SELECT COUNT(*)
        FROM application
        WHERE (:loanTypeId IS NULL OR loan_type_id = :loanTypeId)
        AND (:statusId IS NULL OR status_id = :statusId)
    """)
    Mono<Long> countFilteredApplications(
            @Param("loanTypeId") Integer loanTypeId,
            @Param("statusId") Integer statusId
    );

    Mono<ApplicationEntity> findByEmailAndId(String email, UUID id);

    Flux<ApplicationEntity> findByEmailAndStatusId(String email, Integer statusId);
}

