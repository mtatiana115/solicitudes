package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.ApplicationRequestDTO;
import co.com.bancolombia.api.dto.request.UpdateStatusRequestDTO;
import co.com.bancolombia.api.mapper.ApplicationMapper;
import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.usecase.application.ApplicationListUseCase;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import co.com.bancolombia.usecase.application.UpdateApplicationStatusUseCase;
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

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final ApplicationUseCase applicationUseCase;
    private final ApplicationMapper loanApplicationDTOMapper;
    private final ApplicationListUseCase applicationListUseCase;
    private final RequestValidator requestValidator;
    private final UpdateApplicationStatusUseCase updateApplicationStatusUseCase;

    public Mono<ServerResponse> submitApplicationUseCase(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(ApplicationRequestDTO.class)
                .map(loanApplicationDTOMapper::toModel) //cambiar con builder
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
                .doOnNext(context -> {
                    Authentication authentication = context.getAuthentication();
                    log.info("✅ Usuario autenticado: {}, con rol: {}",
                            authentication.getName(),
                            authentication.getAuthorities());
                })
                .flatMap(context -> {
                    log.info("Listando solicitudes con estado: {}, página: {}, tamaño: {}", status.orElse(null), page, size);
                    return applicationListUseCase.listApplications(page, size, status.orElse(null))
                            .flatMap(applications -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(applications));
                })
                .doOnError(error -> log.error("Error al listar solicitudes: {}", error.getMessage(), error));
    }

    public Mono<ServerResponse> updateApplicationStatusUseCase(ServerRequest request) {
        String id = request.pathVariable("id");

        // Extract token from Authorization header
        Mono<String> tokenMono = extractToken(request)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Authorization header is missing or invalid")));

        return request.bodyToMono(UpdateStatusRequestDTO.class)
                .flatMap(dto -> requestValidator.validate(dto, "Solicitud inválida"))
                .map(dto -> {
                    var model = loanApplicationDTOMapper.toModel(dto);
                    model.setId(id);
                    return model;
                })
                .cast(Application.class)
                .flatMap(updateApplicationStatusUseCase::updateApplicationStatus)
                .doOnSuccess(updated -> log.info("Solicitud actualizada: {}", updated))
                .flatMap(updated ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loanApplicationDTOMapper.toResponse(updated))
                )
                .onErrorResume(e -> {
                    log.error("Error al actualizar la solicitud id={}", id, e);
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("error", e.getMessage()));
                });
    }

    private Mono<String> extractToken(ServerRequest request) {
        return Mono.justOrEmpty(request.headers().firstHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7)) // Remove "Bearer " prefix
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid Authorization header format")));
    }
}
