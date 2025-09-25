package co.com.bancolombia.model.messaging.debtcapacity;

import co.com.bancolombia.model.application.Application;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DebtCapacityEvent {

    private String id;
    private String email;
    private BigDecimal amount;
    private Integer term;
    private Integer loanTypeId;
    private BigDecimal interestRate;
    private BigDecimal salaryClient;
    private String nameClient;
    private List<Application> approvedApplications;
}
