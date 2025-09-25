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

//    public Mono<Application> updateApplicationStatus(Application application) {
//        final String email = application.getEmail();
//        final String id = application.getId();
//        final Integer newStatusId = application.getStatusId();
//
//        return applicationRepository.findByEmailAndId(email, id)
//                .zipWhen(ignored -> userRestConsumer.findUserByEmail(email))
//                    .switchIfEmpty(Mono.error(new IllegalStateException("Prestamo no encontrado:  id=" + id + " email=" + email)))
//                .flatMap(tuple -> {
//                    Application appBD = tuple.getT1();
//                    User user = tuple.getT2();
//
//                    if (appBD.getStatusId().equals(newStatusId)) {
//                        return Mono.error(new IllegalStateException(
//                                "La solicitud ya está en estado " + statusToLabel(newStatusId))
//                        );
//                    }
//
//                    // Actualizar solicitud
//                    appBD.setStatusId(newStatusId);
//                    // Corrección: Usar el documentId del usuario obtenido del servicio externo
//                    appBD.setDocumentId(user.documentId());
//                    appBD.setUpdatedAt(OffsetDateTime.now(ZoneId.of("America/Bogota")));
//
//                    return applicationRepository.save(appBD)
//                            .flatMap(saved -> {
//                                String statusName = statusToLabel(saved.getStatusId());
//
//                                // Solo enviamos notificación si cambia a aprobado o rechazado
//                                if ("approved".equalsIgnoreCase(statusName) || "rejected".equalsIgnoreCase(statusName)) {
//                                    String userFullName = user.name() != null ? user.name() : "";
//
////                                    DecisionEvent event = DecisionEvent.builder()
////                                            .applicationId(saved.getId())
////                                            .status(statusName)
////                                            .email(saved.getEmail())
////                                            .userClient(userFullName)
////                                            .build();
//                                    return loanTypeRepository.findById(saved.getLoanTypeId())
//                                            .flatMap(loanType -> {
//                                                    DecisionEvent event = DecisionEvent.builder()
//                                                        .applicationId(saved.getId())
//                                                        .status("approved")
//                                                        .email(saved.getEmail())
//                                                        .userClient(user.name())
//                                                            .updatedAt(saved.getUpdatedAt())
//                                                            .amount(saved.getAmount())
//                                                            .term(saved.getTerm())
//                                                            .interestRate(loanType.getInterestRate())
//                                                            .build();
//                                    return decisionEventSenderRepository.sendStatusUpdated(event)
//                                            .thenReturn(saved);
//                                            });
//                                } else if("rejected".equalsIgnoreCase(statusName)){
//                                    String userClient = user.name();
//                                    DecisionEvent event = DecisionEvent.builder()
//                                            .applicationId(saved.getId())
//                                            .status("rejected")
//                                            .email(saved.getEmail())
//                                            .userClient(userClient)
//                                            .updatedAt(saved.getUpdatedAt())
//                                            .build();
//                                    return decisionEventSenderRepository.sendStatusUpdated(event)
//                                            .thenReturn(saved);
//                                }
//                                return Mono.just(saved);
//                            });
//                });
//    }



public Mono<Application> updateApplicationStatus(Application request) {
    final String email = request.getEmail();
    final String id = request.getId().toString();
    final Integer newStatusId = request.getStatusId();

    log.info("[USECASE] Request update status -> id={} email={} newStatus={}", id, email, newStatusId);

    if (email == null || id == null || newStatusId == null) {
        return Mono.error(new IllegalArgumentException("id, email and new status must be provided"));
    }

    // 1) Buscar la aplicación; si no existe devolvemos Mono.empty() para que el caller decida qué hacer.
    return applicationRepository.findByEmailAndId(email, UUID.fromString(id))
            // 2) Si existe, obtenemos también el user remoto y construimos una tupla (app, user).
            .flatMap(app -> userRestConsumer.findUserByEmail(email)
                    .map(user -> Tuples.of(app, user)))
            // 3) Si no hay aplicación, devolvemos empty (SQSProcessor ya maneja esto con switchIfEmpty)
            .switchIfEmpty(Mono.empty())
            // 4) Procesamos la actualización
            .flatMap(tuple -> applyUpdateAndNotify(tuple, request));
}

    private Mono<Application> applyUpdateAndNotify(Tuple2<Application, User> tuple, Application request) {
        Application appBD = tuple.getT1();
        User user = tuple.getT2();
        Integer newStatusId = request.getStatusId();

        log.info("[USECASE] Current app status: id={} status={} -> newStatus={}", appBD.getId(), appBD.getStatusId(), newStatusId);

        // Si ya está en el mismo estado, devolvemos error (comportamiento actual)
        if (appBD.getStatusId() != null && appBD.getStatusId().equals(newStatusId)) {
            String msg = "La solicitud ya está en estado " + statusToLabel(newStatusId);
            log.warn("[USECASE] {}", msg);
            return Mono.error(new IllegalStateException(msg));
        }

        // Actualizar campos
        appBD.setStatusId(newStatusId);
        appBD.setDocumentId(user != null ? user.documentId() : appBD.getDocumentId());
        appBD.setUpdatedAt(request.getUpdatedAt() != null ? request.getUpdatedAt() :
                OffsetDateTime.now(ZoneId.of("America/Bogota")));

        // Persistir
        return applicationRepository.save(appBD)
                .flatMap(saved -> {
                    String statusName = statusToLabel(saved.getStatusId());
                    log.info("[USECASE] Application persisted id={} status={} email={}", saved.getId(), saved.getStatusId(), saved.getEmail());

                    // Solo notificamos cuando es approved o rejected
                    if ("approved".equalsIgnoreCase(statusName)) {
                        // Para approved necesitamos interestRate del loanType
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
                                            // *No queremos que fallen las notificaciones rompan la actualización*:
                                            .onErrorResume(e -> Mono.empty())
                                            .thenReturn(saved);
                                })
                                // Si por alguna razón no existe el loanType, aún intentamos enviar un evento sin interestRate
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

                    // Otros estados: no notificar
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
