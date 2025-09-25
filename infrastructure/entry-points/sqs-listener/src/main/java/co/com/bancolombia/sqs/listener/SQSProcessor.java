package co.com.bancolombia.sqs.listener;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.sqs.listener.dto.LoanUpdateMessage;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import co.com.bancolombia.usecase.application.UpdateApplicationStatusUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final UpdateApplicationStatusUseCase updateApplicationStatusUseCase;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> apply(Message message) {
        log.info("[SQS RECV] messageId={} body={}", message.messageId(), message.body());

        return Mono.fromCallable(() -> objectMapper.readValue(message.body(), LoanUpdateMessage.class))
                .flatMap(payload -> {
                    log.info("[SQS PARSED] appId={} newStatus={} email={}",
                            payload.getApplicationId(), payload.getNewStatus(), payload.getEmail());

                    Application updateReq = Application.builder()
                            .id(UUID.fromString(payload.getApplicationId()))
                            .email(payload.getEmail())
                            .statusId(payload.getNewStatus())
                            .updatedAt(payload.getUpdatedAt())
                            .build();

                    return updateApplicationStatusUseCase.updateApplicationStatus(updateReq)
                            .doOnNext(saved -> log.info("[UPDATE OK] id={} status={} email={}",
                                    saved.getId(), saved.getStatusId(), saved.getEmail()))
                            .switchIfEmpty(Mono.fromRunnable(() ->
                                    log.warn("[UPDATE EMPTY] id={} email={} - Application not found",
                                            updateReq.getId(), updateReq.getEmail())))
                            .then();
                })
                .doOnError(err -> log.error("[SQS PROCESS ERROR] messageId={} error={}",
                        message.messageId(), err.getMessage(), err));
    }
}