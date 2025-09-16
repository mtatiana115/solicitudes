package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.constants.DomainConstants;
import co.com.bancolombia.model.exceptions.LoanTypeNotFoundException;
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.status.gateways.StatusRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class ApplicationUseCase {

    private final ApplicationRepository applicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final IUserRestConsumer userRestConsumer;
    private final StatusRepository statusRepository;

    public Mono<Application> ApplyForLoan(Application application) {
        if (application == null) {
            return Mono.error(new IllegalArgumentException(DomainConstants.APPLICATION_CANNOT_BE_NULL));
        }

        if (application.getEmail() == null || application.getEmail().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException(DomainConstants.EMAIL_CANNOT_BE_NULL_OR_EMPTY));
        }

        if (application.getAmount() == null || application.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException(DomainConstants.AMOUNT_MUST_BE_GREATER_THAN_ZERO));
        }

        if (application.getLoanTypeId() == null) {
            return Mono.error(new IllegalArgumentException(DomainConstants.LOAN_TYPE_ID_CANNOT_BE_NULL));
        }

        return userRestConsumer.existsUserByEmail(application.getEmail())
                .flatMap(existsUser -> Boolean.TRUE.equals(existsUser)
                        ? internalManagement(application)
                        : Mono.error(new IllegalArgumentException(DomainConstants.USER_DOESNT_EXIST)));
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