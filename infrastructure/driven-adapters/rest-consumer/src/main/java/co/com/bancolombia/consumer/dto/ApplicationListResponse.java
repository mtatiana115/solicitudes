package co.com.bancolombia.consumer.dto;

import co.com.bancolombia.model.application.auxmodels.ApplicationList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ApplicationListResponse {
    private List<ApplicationList> applications;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
