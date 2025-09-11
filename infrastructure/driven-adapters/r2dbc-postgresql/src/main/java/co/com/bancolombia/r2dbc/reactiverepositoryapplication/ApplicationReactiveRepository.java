package co.com.bancolombia.r2dbc.reactiverepositoryapplication;

import co.com.bancolombia.model.application.dto.ApplicationList;
import co.com.bancolombia.r2dbc.entities.ApplicationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ApplicationReactiveRepository extends ReactiveCrudRepository<ApplicationEntity, String>, ReactiveQueryByExampleExecutor<ApplicationEntity> {

    @Query("""
    SELECT
        a.application_id as id,
        a.amount, a.term, a.email,
        lt.name as loanTypeName,
        s.name as applicationStatus,
        a.document_id as documentId,
        lt.interest_rate as interestRate
        FROM application a
        LEFT JOIN loan_type lt ON a.loan_type_id = lt.loan_type_id
        LEFT JOIN status s ON a.status_id = s.status_id
        WHERE (:statusId IS NULL OR a.status_id = :statusId)
        AND (:documentId IS NULL OR a.document_id = :documentId)
        AND (:loanTypeId IS NULL OR a.loan_type_id = :loanTypeId)
        ORDER BY a.application_id
        LIMIT :limit OFFSET :offset
        """)
        Flux<ApplicationList> filterApplications(
                @Param("offset") int offset,
                @Param("limit") int limit,
                @Param("statusId") Integer statusId,
                @Param("documentId") String documentId,
                @Param("loanTypeId") Integer loanTypeId
        );
    }
