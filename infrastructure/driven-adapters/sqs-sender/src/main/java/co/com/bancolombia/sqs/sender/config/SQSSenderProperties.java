package co.com.bancolombia.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs")
public record SQSSenderProperties(
        String region,
        String notificationQueueUrl,
        String endpoint,
        String debtCapacityQueueUrl){
}
