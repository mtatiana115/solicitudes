package co.com.bancolombia.model.application.auxmodels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DecisionEvent {
    private String applicationId;
    private String status;
    private String email;
    private String userClient;
}
