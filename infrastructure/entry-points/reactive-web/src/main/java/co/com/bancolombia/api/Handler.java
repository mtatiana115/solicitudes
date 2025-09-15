package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.ApplicationRequestDTO;
import co.com.bancolombia.api.mapper.ApplicationMapper;
import co.com.bancolombia.model.application.dto.ApplicationList;
import co.com.bancolombia.usecase.application.ApplicationListUseCase;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
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
    private final ApplicationListUseCase applicationListUseCase;

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

    public Mono<ServerResponse> listApplicationsUseCase(ServerRequest serverRequest) {
        int page = serverRequest.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        Optional<String> status = serverRequest.queryParam("status");

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    Authentication authentication = context.getAuthentication();
                    log.info("Usuario autenticado: {}, con rol: {}",
                            authentication.getName(),
                            authentication.getAuthorities());
                    // Extraer el token del encabezado de la solicitud
                    return serverRequest.headers().firstHeader(HttpHeaders.AUTHORIZATION);
                })
                .flatMap(token -> {
                    log.info("Listando solicitudes con estado: {}, página: {}, tamaño: {}", status.orElse(null), page, size);
                    // Pasar el token al use case
                    return applicationListUseCase.listApplications(page, size, status.orElse(null), token)
                            .flatMap(applications -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(applications))
                            .doOnError(error -> log.error("Error al listar solicitudes: {}", error.getMessage(), error));
                });
    }
}
