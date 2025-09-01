package co.com.bancolombia.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApplicationRequestDTO(
        @NotNull(message = "Amount is required")
        BigDecimal amount,
        @NotNull
        Integer term,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Loan Type ID is required")
        String loanTypeId,

        @NotBlank(message = "Status ID is required")
        String statusId
) {

}
