package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.messaging.debtcapacity.DebtCapacityEvent;
import co.com.bancolombia.model.messaging.debtcapacity.gateways.DebtCapacityMessagingRepository;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebtCapacitySqsAdapter implements DebtCapacityMessagingRepository {

    private final SqsAsyncClient sqs;
    private final ObjectMapper objectMapper;
    private final SQSSenderProperties properties;
    private final SQSSender publisher;

    @Override
    public Mono<String> sendDebtCapacityEvent(DebtCapacityEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .doOnNext(body -> log.info("[CAPACITY->SQS] queue={} payload={}", properties.debtCapacityQueueUrl(), body))
                .flatMap(body -> publisher.send(properties.debtCapacityQueueUrl(), body));
    }
}
