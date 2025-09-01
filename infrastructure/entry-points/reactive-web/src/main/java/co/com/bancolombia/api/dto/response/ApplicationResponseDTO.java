package co.com.bancolombia.api.dto.response;

import java.math.BigDecimal;

public record ApplicationResponseDTO(
        String id,
        BigDecimal amount,
        Integer term,
        String email,
        String loanTypeId,
        String statusId
) {
}
