package co.com.bancolombia.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String rolName;
    private String email;
    private String documentId;
    private String name;
    private BigDecimal baseSalary;
}
