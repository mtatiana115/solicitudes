package co.com.bancolombia.model.messaging.debtcapacity.gateways;

import co.com.bancolombia.model.messaging.debtcapacity.DebtCapacityEvent;
import reactor.core.publisher.Mono;

public interface DebtCapacityMessagingRepository {

    Mono<String> sendDebtCapacityEvent(DebtCapacityEvent event);
}

