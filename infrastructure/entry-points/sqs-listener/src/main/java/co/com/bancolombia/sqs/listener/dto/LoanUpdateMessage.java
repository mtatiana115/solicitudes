package co.com.bancolombia.sqs.listener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanUpdateMessage {
    @JsonProperty("id")
    private String applicationId;

    @JsonProperty("newStatus")
    private Integer newStatus;

    @JsonProperty("email")
    private String email;

    @JsonProperty("userClient")
    private String userClient;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("term")
    private Integer term;

    @JsonProperty("interestRate")
    private BigDecimal interestRate;

    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;
}
