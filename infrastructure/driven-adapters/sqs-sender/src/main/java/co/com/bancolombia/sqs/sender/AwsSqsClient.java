package co.com.bancolombia.sqs.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsSqsClient {

    private final SqsAsyncClient client;

    /**
     * Publica un mensaje en una cola SQS de forma asíncrona.
     *
     * @param queueUrl La URL de la cola donde se publicará el mensaje.
     * @param message  El cuerpo del mensaje.
     * @param attrs    Atributos opcionales del mensaje.
     * @return Un Mono que emite el ID del mensaje publicado.
     */
    public Mono<String> publish(String queueUrl, String message, Map<String, MessageAttributeValue> attrs) {
        log.info("▶️ Publicando mensaje en SQS. Cola: {}, Mensaje: {}", queueUrl, message);

        SendMessageRequest request = buildSendMessageRequest(queueUrl, message, attrs);

        return Mono.fromFuture(client.sendMessage(request))
                .doOnSuccess(response -> log.info("✅ Mensaje publicado con éxito. ID: {}", response.messageId()))
                .doOnError(throwable -> log.error("❌ Falló la publicación del mensaje.", throwable))
                .map(SendMessageResponse::messageId);
    }

    /**
     * Construye el objeto SendMessageRequest con los parámetros dados.
     */
    private SendMessageRequest buildSendMessageRequest(String queueUrl, String message, Map<String, MessageAttributeValue> attrs) {
        SendMessageRequest.Builder builder = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message);

        if (attrs != null && !attrs.isEmpty()) {
            builder.messageAttributes(attrs);
        }

        return builder.build();
    }
}