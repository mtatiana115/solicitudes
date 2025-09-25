package co.com.bancolombia.model.application.gateways;

import co.com.bancolombia.model.messaging.notification.DecisionEvent;
import reactor.core.publisher.Mono;

public interface SendNotificationRepository {
    Mono<String> sendStatusUpdated(DecisionEvent event);
}
