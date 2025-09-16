package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.ApplicationRequestDTO;
import co.com.bancolombia.api.dto.response.ApplicationResponseDTO;
import co.com.bancolombia.model.application.dto.ApplicationList;
import co.com.bancolombia.model.application.dto.ApplicationListResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
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
                            security = { @SecurityRequirement(name = "bearerAuth") },
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
            ),
            @RouterOperation(
                    path = "/api/v1/applications",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listApplicationsUseCase",
                    operation = @Operation(
                            operationId = "listApplications",
                            summary = "List loan applications with pagination and status filter",
                            security = { @SecurityRequirement(name = "bearerAuth") },
                            parameters = {
                                    @Parameter(name = "page", description = "Page number", example = "0"),
                                    @Parameter(name = "size", description = "Page size", example = "10"),
                                    @Parameter(name = "status", description = "Filter by application status", example = "pending review")
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successfully retrieved list of applications",
                                            content = @Content(schema = @Schema(implementation = ApplicationListResponse.class))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Invalid parameters"),
                                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            )
    })

    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitudes"), handler::submitApplicationUseCase)
                .andRoute(GET("/api/v1/applications"), handler::listApplicationsUseCase);
    }
}

