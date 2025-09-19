package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.auxmodels.DecisionEvent;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.application.gateways.DecisionEventSenderRepository;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.auth.User;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateApplicationStatusUseCase{

    private final ApplicationRepository applicationRepository;
    private final DecisionEventSenderRepository decisionEventSenderRepository;
    private final IUserRestConsumer userRestConsumer;

    public Mono<Application> updateApplicationStatus(Application application) {
        final String email = application.getEmail();
        final String id = application.getId();
        final Integer newStatusId = application.getStatusId();

        return applicationRepository.findByEmailAndId(email, id)
                .zipWhen(ignored -> userRestConsumer.findUserByEmail(email))
                .flatMap(tuple -> {
                    Application appBD = tuple.getT1();
                    User user = tuple.getT2();

                    if (appBD.getStatusId().equals(newStatusId)) {
                        return Mono.error(new IllegalStateException(
                                "La solicitud ya está en estado " + statusToLabel(newStatusId.longValue())
                        ));
                    }

                    // Actualizar solicitud
                    appBD.setStatusId(newStatusId);
                    // Corrección: Usar el documentId del usuario obtenido del servicio externo
                    appBD.setDocumentId(user.documentId());

                    return applicationRepository.save(appBD)
                            .flatMap(saved -> {
                                String statusName = statusToLabel(saved.getStatusId().longValue());

                                // Solo enviamos notificación si cambia a aprobado o rechazado
                                if ("approved".equalsIgnoreCase(statusName) || "rejected".equalsIgnoreCase(statusName)) {
                                    String userFullName = user.name() != null ? user.name() : "";

                                    DecisionEvent event = DecisionEvent.builder()
                                            .applicationId(saved.getId())
                                            .status(statusName)
                                            .email(saved.getEmail())
                                            .userClient(userFullName)
                                            .build();

                                    return decisionEventSenderRepository.sendStatusUpdated(event)
                                            .thenReturn(saved);
                                }

                                return Mono.just(saved);
                            });
                });
    }

    private static String statusToLabel(Long statusId) {
        if (statusId == null) return "unknown";
        return switch (statusId.intValue()) {
            case 1 -> "pending review";
            case 2 -> "rejected";
            case 3 -> "manual review";
            case 4 -> "approved";
            default -> "unknown";
        };
    }
}
