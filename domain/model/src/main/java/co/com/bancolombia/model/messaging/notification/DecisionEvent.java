package co.com.bancolombia.model.messaging.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DecisionEvent {
    private String applicationId;
    private String status;
    private String email;
    private String userClient;
    private OffsetDateTime updatedAt;
    private BigDecimal amount;
    private Integer term;
    private BigDecimal interestRate;
}
