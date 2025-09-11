package co.com.bancolombia.model.auth;

import java.math.BigDecimal;

public record User(
        String rolName,
        String email,
        String documentId,
        String name,
        BigDecimal baseSalary
) {
}
