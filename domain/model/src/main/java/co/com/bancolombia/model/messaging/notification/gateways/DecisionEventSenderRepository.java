package co.com.bancolombia.model.messaging.notification.gateways;

import co.com.bancolombia.model.messaging.notification.DecisionEvent;
import reactor.core.publisher.Mono;

public interface DecisionEventSenderRepository {
    Mono<String> sendStatusUpdated(DecisionEvent event);
}
