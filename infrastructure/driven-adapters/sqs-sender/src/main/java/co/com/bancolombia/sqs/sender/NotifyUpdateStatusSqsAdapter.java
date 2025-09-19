package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.application.auxmodels.DecisionEvent;
import co.com.bancolombia.model.application.gateways.DecisionEventSenderRepository;
import co.com.bancolombia.model.application.gateways.SendNotificationRepository;

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

    @Override
    public Mono<String> sendStatusUpdated(DecisionEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(publisher::send);
    }



}
