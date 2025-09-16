package co.com.bancolombia.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
@Builder
public class ApplicationResponseDTO {
    private String id;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private Integer loanTypeId;
    private Integer statusId;
    private String documentId;
}
