package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.application.dto.ApplicationFilterIds;
import co.com.bancolombia.model.application.dto.ApplicationList;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.exceptions.ExternalServiceCommunicationException;
import co.com.bancolombia.model.exceptions.LoanTypeNotFoundException;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
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

    public Flux<ApplicationList> listApplications(int page, int size, ApplicationFilter filter) {
        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        int offset = p * s;

        return convertFilterNamesToIds(filter)
                .flatMapMany(idsFilter ->
                        applicationRepository.filterApplications(offset, s, idsFilter)
                                .flatMap(applicationList ->
                                        userRestConsumer.findUserByEmail(applicationList.email())
                                                .filter(Objects::nonNull)
                                                .map(user -> {
                                                    BigDecimal approvedMonthlyDebtTotal =
                                                            "Approved".equals(applicationList.applicationStatus())
                                                                    ? annuityPayment(
                                                                    applicationList.amount(),
                                                                    applicationList.interestRate(),
                                                                    applicationList.term()
                                                            )
                                                                    : BigDecimal.ZERO;

                                                    return new ApplicationList(
                                                            applicationList.id(),
                                                            applicationList.amount(),
                                                            applicationList.term(),
                                                            applicationList.email(),
                                                            applicationList.loanTypeName(),
                                                            applicationList.applicationStatus(),
                                                            applicationList.documentId(),
                                                            user.name(),
                                                            applicationList.interestRate(),
                                                            approvedMonthlyDebtTotal,
                                                            user.baseSalary()
                                                    );
                                                })
                                )
                );
    }

    private Mono<ApplicationFilterIds> convertFilterNamesToIds(ApplicationFilter filter) {
        return Mono.zip(
                convertStatusNameToId(filter.getStatus()),
                convertLoanTypeNameToId(filter.getLoanType())
        ).map(tuple -> new ApplicationFilterIds(
                tuple.getT1(),
                tuple.getT2(),
                filter.getDocumentId()
        ));
    }

    private Mono<Integer> convertStatusNameToId(String statusName) {
        if (statusName == null) {
            return Mono.just(null);
        }
        return statusRepository.findIdByName(statusName)
                .defaultIfEmpty(null);
    }

    private Mono<Integer> convertLoanTypeNameToId(String loanTypeName) {
        if (loanTypeName == null) {
            return Mono.just(null);
        }
        return loanTypeRepository.findIdByName(loanTypeName)
                .defaultIfEmpty(null);
    }

    private static BigDecimal annuityPayment(BigDecimal amount, BigDecimal monthlyRate, Integer months) {
        if (amount == null || monthlyRate == null || months == null || months <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal P = amount;
        BigDecimal i = monthlyRate;
        int n = months;

        if (P.compareTo(BigDecimal.ZERO) <= 0 || n <= 0) {
            return BigDecimal.ZERO;
        }
        if (i.compareTo(BigDecimal.ZERO) == 0) {
            return P.divide(BigDecimal.valueOf(n), 2, java.math.RoundingMode.HALF_UP);
        }

        BigDecimal onePlusI = BigDecimal.ONE.add(i);
        BigDecimal factor = onePlusI.pow(n);
        BigDecimal numerator = P.multiply(i).multiply(factor);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, java.math.RoundingMode.HALF_UP);
    }
}