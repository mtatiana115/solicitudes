package co.com.bancolombia.model.application;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Application {

    private UUID id;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private Integer loanTypeId;
    private Integer statusId;
    private String documentId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
