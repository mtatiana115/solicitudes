package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.auth.User;
import co.com.bancolombia.model.exceptions.LoanTypeNotFoundException;
import co.com.bancolombia.model.exceptions.UnchangedStatusApplicationsException;
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.messaging.debtcapacity.DebtCapacityEvent;
import co.com.bancolombia.model.messaging.debtcapacity.gateways.DebtCapacityMessagingRepository;
import co.com.bancolombia.model.messaging.notification.DecisionEvent;
import co.com.bancolombia.model.messaging.notification.gateways.DecisionEventSenderRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@RequiredArgsConstructor
public class ApplicationUseCase {
    private final ApplicationRepository applicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final IUserRestConsumer userRestConsumer;
    private final DebtCapacityMessagingRepository debtCapacityMessagingRepository;
    private final DecisionEventSenderRepository decisionEventSenderRepository;

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

//    private Mono<Application> internalManagement(Application application) {
//        return Mono.just(application)
//                .flatMap(loan -> validateLoanType(application.getLoanTypeId())
//                        .thenReturn(loan))
//                .flatMap(applicationRepository::save);
//    }

    private Mono<Application> internalManagement(Application application) {
        return loanTypeRepository.findById(application.getLoanTypeId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("The loan type does not exist.")))
                .flatMap(loanType -> {
                    application.setCreatedAt(OffsetDateTime.now(ZoneId.of("America/Bogota")));
                    return applicationRepository.save(application)
                            .flatMap(saved -> onAutomaticValidation(saved, loanType).thenReturn(saved));
                });
    }

    private Mono<Void> validateLoanType(Integer id) {
        return loanTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException(id)))
                .then();
    }

    private Mono<Void> onAutomaticValidation(Application saved, LoanType loanType) {
        if (loanType.getAutomaticValidation() == null || !loanType.getAutomaticValidation()) {
            return Mono.empty();
        }
        final String email = saved.getEmail();
        final Integer approvedStatus = 4;

        return applicationRepository.findByEmailAndStatusId(email, approvedStatus)
                .collectList()
                .flatMap(approvedList -> {
                    return userRestConsumer.findUserByEmail(email)
                            .flatMap(user -> {
                                DebtCapacityEvent event = DebtCapacityEvent.builder()
                                        .id(saved.getId().toString())
                                        .email(email)
                                        .amount(saved.getAmount())
                                        .term(saved.getTerm())
                                        .loanTypeId(saved.getLoanTypeId())
                                        .interestRate(loanType.getInterestRate())
                                        .salaryClient(user.baseSalary())
                                        .nameClient(user.name() != null ? user.name() : saved.getEmail())
                                        .approvedApplications(approvedList)
                                        .build();

                                return debtCapacityMessagingRepository.sendDebtCapacityEvent(event)
                                        .then();
                            });
                });
    }

//    public Mono<Application> update(Application application) {
//        final String email = application.getEmail();
//        final String id = application.getId();
//        final Integer newStatusId = application.getStatusId();
//
//        return applicationRepository.findByEmailAndId(email, id)
//                .zipWhen(ignored -> userRestConsumer.findUserByEmail(email))
//                .flatMap(tuple -> {
//                    Application appliationBd = tuple.getT1();
//                    User userClientDetails = tuple.getT2();
//                    if (appliationBd.getStatusId().equals(newStatusId)) {
//                        return Mono.error(new UnchangedStatusApplicationsException("The loan application is already in a status " + newStatusId));
//                    }
//                    appliationBd.setStatusId(newStatusId);
//                    appliationBd.setUpdatedAt(OffsetDateTime.now(ZoneId.of("America/Bogota")));
//                    return applicationRepository.save(appliationBd)
//
//                            .flatMap(saved -> {
//                                String statusName = statusToLabel(saved.getStatusId());
//                                if ("APPROVED".equalsIgnoreCase(statusName) || "REJECTED".equalsIgnoreCase(statusName)) {
//                                    String userClient = userClientDetails.name();
//
//                                    DecisionEvent msg = DecisionEvent.builder()
//                                            .applicationId(saved.getId())
//                                            .status(statusName.equalsIgnoreCase("APPROVED") ? "approved" : "rejected")
//                                            .email(saved.getEmail())
//                                            .userClient(userClient)
//                                            .build();
//                                    return decisionEventSenderRepository.sendStatusUpdated(msg)
//                                            .thenReturn(saved);
//                                }
//                                return Mono.just(saved);
//                            });
//                });
//    }
//
//    private static String statusToLabel(Integer statusId) {
//        if (statusId == null) return "Unknown";
//        return switch (statusId) {
//            case 1 -> "pending review";
//            case 2 -> "rejected";
//            case 3 -> "manual review";
//            case 4 -> "approved";
//            default -> "unknown";
//        };
//    }
}