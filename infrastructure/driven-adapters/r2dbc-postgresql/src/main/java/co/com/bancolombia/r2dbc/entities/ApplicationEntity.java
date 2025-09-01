package co.com.bancolombia.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("application")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class ApplicationEntity {
    @Id
    @Column("application_id")
    private String id;
    private BigDecimal amount;
    private Integer term;
    private String email;
    @Column("loan_type_id")
    private Integer loanTypeId;
    @Column("status_id")
    private Integer statusId;

}
