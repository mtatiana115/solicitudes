package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.ApplicationRequestDTO;
import co.com.bancolombia.api.mapper.ApplicationMapper;
import co.com.bancolombia.model.application.dto.ApplicationFilter;
import co.com.bancolombia.model.application.dto.ApplicationList;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final ApplicationUseCase applicationUseCase;
    private final ApplicationMapper loanApplicationDTOMapper;

    public Mono<ServerResponse> submitApplicationUseCase(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(ApplicationRequestDTO.class)
                .map(loanApplicationDTOMapper::toModel)
                .flatMap(applicationReq -> {
                    log.info("Application submitted: {}", applicationReq.toString());
                    return applicationUseCase.ApplyForLoan(applicationReq)
                            .doOnSuccess(saved -> log.info("Application saved: {}", saved.toString()));
                })
                .flatMap(savedApplication -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loanApplicationDTOMapper.toResponse(savedApplication)));
    }

//    @PreAuthorize("hasRole('')
    public Mono<ServerResponse> listApplicationsUseCase(ServerRequest serverRequest) {
        int page = serverRequest.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        Optional<String> status = serverRequest.queryParam("status");
        Optional<String> documentId = serverRequest.queryParam("documentId");
        Optional<String> loanType = serverRequest.queryParam("loanType");

        ApplicationFilter filter = ApplicationFilter.builder()
                .status(status.orElse(null))
                .documentId(documentId.orElse(null))
                .loanType(loanType.orElse(null))
                .build();

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(applicationUseCase.listApplications(page, size, filter),ApplicationList.class);
    }
}
