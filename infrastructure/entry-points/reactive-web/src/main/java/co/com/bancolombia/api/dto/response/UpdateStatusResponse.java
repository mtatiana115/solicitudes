package co.com.bancolombia.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusResponse {
    private String id;
    private String newStatus;
    private String message = "Estado actualizado exitosamente.";
}
