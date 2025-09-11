package co.com.bancolombia.model.application.dto;

import java.math.BigDecimal;

public record ApplicationList(
        String id,
        BigDecimal amount,
        Integer term,
        String email,
        String loanTypeName,
        String applicationStatus,
        String documentId,
        String userName,
        BigDecimal interestRate,
        BigDecimal totalApprovedMonthlyDebt,
        BigDecimal baseSalary
) {
}
