package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.ApplicationRequestDTO;
import co.com.bancolombia.api.dto.response.ApplicationResponseDTO;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitudes",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "submitApplicationUseCase",
                    operation = @Operation(
                            operationId = "createApplication",
                            summary = "Submit a new loan application",
                            description = "Submits a new loan application and returns the application details",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = ApplicationRequestDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Application successfully submitted",
                                            content = @Content(schema = @Schema(implementation = ApplicationResponseDTO.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                                    @ApiResponse(responseCode = "404", description = "Loan type not found"),
                                    @ApiResponse(responseCode = "409", description = "Conflict (duplicate application)")
                            }
                    )
            )
    })

    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitudes"), handler::submitApplicationUseCase);
    }
}

