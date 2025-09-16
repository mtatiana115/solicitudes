package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.dto.ApplicationList;
import co.com.bancolombia.model.application.dto.ApplicationListResponse;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.auth.User;
import co.com.bancolombia.model.exceptions.LoanTypeNotFoundException;
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.status.Status;
import co.com.bancolombia.model.status.gateways.StatusRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;


@RequiredArgsConstructor
public class ApplicationUseCase {
    private final ApplicationRepository applicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final IUserRestConsumer userRestConsumer;
    private final StatusRepository statusRepository;

    public Mono<Application> ApplyForLoan(Application application) {
        if (application == null) {
            return Mono.error(new IllegalArgumentException("Application cannot be null"));
        }

        if (application.getEmail() == null || application.getEmail().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Email cannot be null or empty"));
        }

        if (application.getAmount() == null || application.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("Amount must be greater than zero"));
        }

        if (application.getLoanTypeId() == null) {
            return Mono.error(new IllegalArgumentException("Loan type ID cannot be null"));
        }

        return userRestConsumer.existsUserByEmail(application.getEmail())
                .flatMap(existsUser -> Boolean.TRUE.equals(existsUser)
                        ? internalManagement(application)
                        : Mono.error(new IllegalArgumentException("User doesn't exist!")));
    }

    private Mono<Application> internalManagement(Application application) {
        return Mono.just(application)
                .flatMap(loan -> validateLoanType(application.getLoanTypeId())
                        .thenReturn(loan))
                .flatMap(applicationRepository::save);
    }

    private Mono<Void> validateLoanType(Integer id) {
        return loanTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException(id)))
                .then();
    }
}