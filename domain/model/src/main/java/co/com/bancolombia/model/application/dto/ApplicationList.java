package co.com.bancolombia.model.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class ApplicationList {

    private String id;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private String loanTypeName;
    private String applicationStatus;
    private String documentId;
    private String userName;
    private BigDecimal interestRate;
    private BigDecimal totalApprovedMonthlyDebt;
    private BigDecimal baseSalary;
}
