package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.messaging.notification.DecisionEvent;
import co.com.bancolombia.model.messaging.notification.gateways.DecisionEventSenderRepository;

import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotifyUpdateStatusSqsAdapter implements DecisionEventSenderRepository {

    private final ObjectMapper objectMapper;
    private final SQSSender publisher;
    private final SQSSenderProperties properties;

    @Override
    public Mono<String> sendStatusUpdated(DecisionEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .doOnNext(body -> log.info("[NOTIF->SQS] queue={} payload={}", properties.notificationQueueUrl(), body))
                .flatMap(body -> publisher.send(properties.notificationQueueUrl(), body));
    }
}
