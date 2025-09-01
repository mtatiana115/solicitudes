package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.ApplicationRequestDTO;
import co.com.bancolombia.api.mapper.ApplicationMapper;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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
}
