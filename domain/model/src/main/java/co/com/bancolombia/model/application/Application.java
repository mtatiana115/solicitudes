package co.com.bancolombia.model.application;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Application {

    private String id;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private Integer loanTypeId;
    private Integer statusId;
    private String documentId;
}
