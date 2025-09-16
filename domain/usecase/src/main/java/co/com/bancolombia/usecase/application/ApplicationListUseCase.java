package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.dto.ApplicationList;
import co.com.bancolombia.model.application.dto.ApplicationListResponse;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.auth.User;
import co.com.bancolombia.model.constants.DomainConstants;
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
public class ApplicationListUseCase {

    private final ApplicationRepository applicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final IUserRestConsumer userRestConsumer;

    public Mono<ApplicationListResponse> listApplications(int page, int size, String statusName, String token) {
        int validatedPage = Math.max(page, 0);
        int validatedSize = Math.max(size, 1);
        int offset = validatedPage * validatedSize;
        Integer statusId = statusToId(statusName);
        Integer loanTypeId = null;

        Mono<Long> totalElementsMono = applicationRepository.countFilteredApplications(loanTypeId, statusId);
        Flux<Application> applicationsFlux = applicationRepository.filterApplications(offset, validatedSize, loanTypeId, statusId);

        return Mono.zip(applicationsFlux.collectList(), totalElementsMono)
                .flatMap(tuple -> {
                    List<Application> applications = tuple.getT1();
                    Long totalElements = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) totalElements / validatedSize);

                    Flux<ApplicationList> enrichedApplicationsFlux = Flux.fromIterable(applications)
                            .flatMap(app -> Mono.zip(
                                    loanTypeRepository.findById(app.getLoanTypeId()),
                                    userRestConsumer.findUserByEmail(app.getEmail(), token)
                                            .onErrorResume(e -> Mono.just(new User(
                                                    DomainConstants.NOT_AVAILABLE,
                                                    app.getEmail(),
                                                    DomainConstants.NOT_AVAILABLE,
                                                    DomainConstants.UNKNOWN_USER_NAME,
                                                    BigDecimal.ZERO
                                            )))
                            ).map(dataTuple -> {
                                LoanType loanType = dataTuple.getT1();
                                User user = dataTuple.getT2();
                                String statusLabel = statusToLabel(app.getStatusId());
                                BigDecimal approvedMonthlyDebtTotal = calculateAnnuityPayment(
                                        app.getAmount(),
                                        loanType.getInterestRate(),
                                        app.getTerm()
                                );

                                return new ApplicationList(
                                        app.getId(),
                                        app.getAmount(),
                                        app.getTerm(),
                                        app.getEmail(),
                                        loanType.getName(),
                                        statusLabel,
                                        app.getDocumentId(),
                                        user.name(),
                                        loanType.getInterestRate(),
                                        approvedMonthlyDebtTotal,
                                        user.baseSalary()
                                );
                            }));

                    return enrichedApplicationsFlux.collectList()
                            .map(applicationList -> ApplicationListResponse.builder()
                                    .applications(applicationList)
                                    .currentPage(page)
                                    .totalPages(totalPages)
                                    .totalElements(totalElements)
                                    .build());
                });
    }

    private static String statusToLabel(Integer statusId) {
        if (statusId == null) return DomainConstants.UNKNOWN_STATUS;
        return switch (statusId) {
            case DomainConstants.PENDING_REVIEW_ID -> DomainConstants.PENDING_REVIEW_LABEL;
            case DomainConstants.REJECTED_ID -> DomainConstants.REJECTED_LABEL;
            case DomainConstants.MANUAL_REVIEW_ID -> DomainConstants.MANUAL_REVIEW_LABEL;
            case DomainConstants.APPROVED_ID -> DomainConstants.APPROVED_LABEL;
            default -> DomainConstants.UNKNOWN_STATUS;
        };
    }

    private static Integer statusToId(String statusName) {
        if (statusName == null) return null;
        return switch (statusName.toLowerCase()) {
            case DomainConstants.PENDING_REVIEW -> DomainConstants.PENDING_REVIEW_ID;
            case DomainConstants.REJECTED -> DomainConstants.REJECTED_ID;
            case DomainConstants.MANUAL_REVIEW -> DomainConstants.MANUAL_REVIEW_ID;
            case DomainConstants.APPROVED -> DomainConstants.APPROVED_ID;
            default -> null;
        };
    }

    private static BigDecimal calculateAnnuityPayment(BigDecimal amount, BigDecimal monthlyRate, Integer months) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || months == null || months <= 0) {
            return BigDecimal.ZERO;
        }

        if (monthlyRate == null || monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return amount.divide(BigDecimal.valueOf(months), RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal factor = onePlusRate.pow(months);
        BigDecimal numerator = amount.multiply(monthlyRate).multiply(factor);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}