package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.LoggerRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.messaging.notification.DecisionEvent;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.messaging.notification.gateways.DecisionEventSenderRepository;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.auth.User;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@RequiredArgsConstructor
public class UpdateApplicationStatusUseCase{

    private final ApplicationRepository applicationRepository;
    private final DecisionEventSenderRepository decisionEventSenderRepository;
    private final IUserRestConsumer userRestConsumer;
    private final LoanTypeRepository loanTypeRepository;
    private final LoggerRepository log;

public Mono<Application> updateApplicationStatus(Application request) {
    final String email = request.getEmail();
    final String id = request.getId().toString();
    final Integer newStatusId = request.getStatusId();

    log.info("[USECASE] Request update status -> id={} email={} newStatus={}", id, email, newStatusId);

    if (email == null || id == null || newStatusId == null) {
        return Mono.error(new IllegalArgumentException("id, email and new status must be provided"));
    }

    return applicationRepository.findByEmailAndId(email, UUID.fromString(id))
            .flatMap(app -> userRestConsumer.findUserByEmail(email)
                    .map(user -> Tuples.of(app, user)))
            .switchIfEmpty(Mono.empty())
            .flatMap(tuple -> applyUpdateAndNotify(tuple, request));
}

    private Mono<Application> applyUpdateAndNotify(Tuple2<Application, User> tuple, Application request) {
        Application appBD = tuple.getT1();
        User user = tuple.getT2();
        Integer newStatusId = request.getStatusId();

        log.info("[USECASE] Current app status: id={} status={} -> newStatus={}", appBD.getId(), appBD.getStatusId(), newStatusId);

        if (appBD.getStatusId() != null && appBD.getStatusId().equals(newStatusId)) {
            String msg = "La solicitud ya está en estado " + statusToLabel(newStatusId);
            log.warn("[USECASE] {}", msg);
            return Mono.error(new IllegalStateException(msg));
        }

        appBD.setStatusId(newStatusId);
        appBD.setDocumentId(user != null ? user.documentId() : appBD.getDocumentId());
        appBD.setUpdatedAt(request.getUpdatedAt() != null ? request.getUpdatedAt() :
                OffsetDateTime.now(ZoneId.of("America/Bogota")));

        return applicationRepository.save(appBD)
                .flatMap(saved -> {
                    String statusName = statusToLabel(saved.getStatusId());
                    log.info("[USECASE] Application persisted id={} status={} email={}", saved.getId(), saved.getStatusId(), saved.getEmail());

                    // Solo notificamos cuando es approved o rejected
                    if ("approved".equalsIgnoreCase(statusName)) {
                        return loanTypeRepository.findById(saved.getLoanTypeId())
                                .flatMap(loanType -> {
                                    DecisionEvent event = DecisionEvent.builder()
                                            .applicationId(saved.getId().toString())
                                            .status(statusName)
                                            .email(saved.getEmail())
                                            .userClient(user != null ? user.name() : "")
                                            .updatedAt(saved.getUpdatedAt())
                                            .amount(saved.getAmount())
                                            .term(saved.getTerm())
                                            .interestRate(loanType.getInterestRate())
                                            .build();

                                    return decisionEventSenderRepository.sendStatusUpdated(event)
                                            .doOnSuccess(mid -> log.info("[USECASE] DecisionEvent sent to notifier msgId={}", mid))
                                            .doOnError(e -> log.error("[USECASE] Failed to send DecisionEvent (approved)", e.getMessage(), e))

                                            .onErrorResume(e -> Mono.empty())
                                            .thenReturn(saved);
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.warn("[USECASE] LoanType not found for id={}", saved.getLoanTypeId());
                                    DecisionEvent event = DecisionEvent.builder()
                                            .applicationId(saved.getId().toString())
                                            .status(statusName)
                                            .email(saved.getEmail())
                                            .userClient(user != null ? user.name() : "")
                                            .updatedAt(saved.getUpdatedAt())
                                            .amount(saved.getAmount())
                                            .term(saved.getTerm())
                                            .build();

                                    return decisionEventSenderRepository.sendStatusUpdated(event)
                                            .doOnError(e -> log.error("[USECASE] Failed to send DecisionEvent (approved, no loanType)", e.getMessage(), e))
                                            .onErrorResume(e -> Mono.empty())
                                            .thenReturn(saved);
                                }));
                    } else if ("rejected".equalsIgnoreCase(statusName)) {
                        DecisionEvent event = DecisionEvent.builder()
                                .applicationId(saved.getId().toString())
                                .status(statusName)
                                .email(saved.getEmail())
                                .userClient(user != null ? user.name() : "")
                                .updatedAt(saved.getUpdatedAt())
                                .build();

                        return decisionEventSenderRepository.sendStatusUpdated(event)
                                .doOnSuccess(mid -> log.info("[USECASE] DecisionEvent sent to notifier msgId={}", mid))
                                .doOnError(e -> log.error("[USECASE] Failed to send DecisionEvent (rejected)", e.getMessage(), e))
                                .onErrorResume(e -> Mono.empty())
                                .thenReturn(saved);
                    }

                    return Mono.just(saved);
                });
    }


    private static String statusToLabel(Integer statusId) {
        if (statusId == null) return "unknown";
        return switch (statusId) {
            case 1 -> "pending review";
            case 2 -> "rejected";
            case 3 -> "manual review";
            case 4 -> "approved";
            default -> "unknown";
        };
    }
}
