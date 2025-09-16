package co.com.bancolombia.config;

import co.com.bancolombia.r2dbc.entities.LoanTypeEntity;
import co.com.bancolombia.r2dbc.entities.StatusEntity;
import co.com.bancolombia.r2dbc.reactiverepositoryloantype.LoanTypeReactiveRepository;
import co.com.bancolombia.r2dbc.reactiverepositorystatus.StatusReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final StatusReactiveRepository statusRepository;
    private final LoanTypeReactiveRepository loanTypeRepository;

    @Bean
    public ApplicationRunner seeder() {
        return args -> {
            initializeStatuses()
                    .then(initializeLoanTypes())
                    .subscribe(
                            v -> log.info("✅ Solicitudes data initialization completed successfully"),
                            error -> log.error("❌ Error during solicitudes data initialization: {}", error.getMessage())
                    );
        };
    }

    private Mono<Void> initializeStatuses() {
        List<StatusEntity> defaultStatuses = Arrays.asList(
                new StatusEntity(1, "pending review", "Waiting for review"),
                new StatusEntity(2, "rejected", "Application rejected"),
                new StatusEntity(3, "manual review", "Needs manual review"),
                new StatusEntity(4, "approved", "Application approved")
        );

        return Flux.fromIterable(defaultStatuses)
                .flatMap(status ->
                        statusRepository.findById(status.getId())
                                .hasElement()
                                .flatMap(exists -> {
                                    if (!exists) {
                                        return statusRepository.save(status)
                                                .doOnSuccess(s -> log.info("Status created: {}", s.getName()))
                                                .then();
                                    } else {
                                        log.info("Status already exists: {}", status.getName());
                                        return Mono.empty();
                                    }
                                })
                )
                .then()
                .doOnSuccess(v -> log.info("Statuses verification completed"));
    }

    private Mono<Void> initializeLoanTypes() {
        List<LoanTypeEntity> defaultLoanTypes = Arrays.asList(
                new LoanTypeEntity(1, "Personal Loan", BigDecimal.valueOf(1000000), BigDecimal.valueOf(20000000), BigDecimal.valueOf(1.5), true),
                new LoanTypeEntity(2, "Vehicle Loan", BigDecimal.valueOf(5000000), BigDecimal.valueOf(50000000), BigDecimal.valueOf(1.2), true),
                new LoanTypeEntity(3, "Mortgage Loan", BigDecimal.valueOf(50000000), BigDecimal.valueOf(500000000),BigDecimal.valueOf(0.9), false)
        );

        return Flux.fromIterable(defaultLoanTypes)
                .flatMap(loanType ->
                        loanTypeRepository.findById(loanType.getId())
                                .hasElement()
                                .flatMap(exists -> {
                                    if (!exists) {
                                        return loanTypeRepository.save(loanType)
                                                .doOnSuccess(l -> log.info("Loan type created: {}", l.getName()))
                                                .then();
                                    } else {
                                        log.info("Loan type already exists: {}", loanType.getName());
                                        return Mono.empty();
                                    }
                                })
                )
                .then()
                .doOnSuccess(v -> log.info("Loan types verification completed"));
    }

}
