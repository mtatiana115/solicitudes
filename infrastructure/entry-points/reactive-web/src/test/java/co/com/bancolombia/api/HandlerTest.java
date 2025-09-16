package co.com.bancolombia.api;

//import co.com.bancolombia.api.dto.request.ApplicationRequestDTO;
//import co.com.bancolombia.api.dto.response.ApplicationResponseDTO;
//import co.com.bancolombia.api.mapper.ApplicationMapper;
//import co.com.bancolombia.model.application.Application;
//import co.com.bancolombia.model.application.dto.ApplicationListResponse;
//import co.com.bancolombia.usecase.application.ApplicationListUseCase;
//import co.com.bancolombia.usecase.application.ApplicationUseCase;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
//import org.springframework.mock.web.server.MockServerWebExchange;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextImpl;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.math.BigDecimal;
//import java.util.Collections;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//import static org.mockito.Mockito.verify;

//@ExtendWith(MockitoExtension.class)
class HandlerTest {
//
//    @Mock
//    private ApplicationUseCase applicationUseCase;
//
//    @Mock
//    private ApplicationMapper loanApplicationDTOMapper; // Cambiar nombre para coincidir con el Handler
//
//    @Mock
//    private ApplicationListUseCase applicationListUseCase;
//
//    @InjectMocks
//    private Handler handler;
//
//    private ApplicationRequestDTO applicationRequestDTO;
//    private Application application;
//    private Application savedApplication;
//    private ApplicationResponseDTO applicationResponseDTO;
//
//    @BeforeEach
//    void setUp() {
//        applicationRequestDTO = new ApplicationRequestDTO(
//                BigDecimal.valueOf(10000000),
//                12,
//                "test@test.com",
//                1,
//                "123456789"
//        );
//
//        application = Application.builder()
//                .amount(BigDecimal.valueOf(10000000))
//                .term(12)
//                .email("test@test.com")
//                .loanTypeId(1)
//                .documentId("123456789")
//                .build();
//
//        savedApplication = Application.builder()
//                .id("1")
//                .amount(BigDecimal.valueOf(10000000))
//                .term(12)
//                .email("test@test.com")
//                .loanTypeId(1)
//                .documentId("123456789")
//                .statusId(1)
//                .build();
//
//        applicationResponseDTO = ApplicationResponseDTO.builder()
//                .id("1")
//                .amount(BigDecimal.valueOf(10000000))
//                .term(12)
//                .email("test@test.com")
//                .loanTypeId(1)
//                .statusId(1)
//                .documentId("123456789")
//                .build();
//    }
//
//    @Test
//    void submitApplicationUseCase_WhenValidRequest_ShouldReturnCreated() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.post("/applications")
//                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
//                .body("{\"amount\":10000000,\"term\":12,\"email\":\"test@test.com\",\"loanTypeId\":1,\"documentId\":\"123456789\"}");
//
//        ServerRequest serverRequest = ServerRequest.create(
//                MockServerWebExchange.from(request),
//                Collections.emptyList()
//        );
//
//        when(loanApplicationDTOMapper.toModel(any(ApplicationRequestDTO.class))).thenReturn(application);
//        when(applicationUseCase.ApplyForLoan(any(Application.class))).thenReturn(Mono.just(savedApplication));
//        when(loanApplicationDTOMapper.toResponse(any(Application.class))).thenReturn(applicationResponseDTO);
//
//        // Act & Assert
//        StepVerifier.create(handler.submitApplicationUseCase(serverRequest))
//                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.CREATED)
//                .verifyComplete();
//
//        verify(loanApplicationDTOMapper).toModel(any(ApplicationRequestDTO.class));
//        verify(applicationUseCase).ApplyForLoan(application);
//        verify(loanApplicationDTOMapper).toResponse(savedApplication);
//    }
//
//    @Test
//    void submitApplicationUseCase_WhenInvalidRequest_ShouldPropagateError() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.post("/applications")
//                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
//                .body("{\"amount\":10000000,\"term\":12,\"email\":\"test@test.com\",\"loanTypeId\":1,\"documentId\":\"123456789\"}");
//
//        ServerRequest serverRequest = ServerRequest.create(
//                MockServerWebExchange.from(request),
//                Collections.emptyList()
//        );
//
//        when(loanApplicationDTOMapper.toModel(any(ApplicationRequestDTO.class))).thenReturn(application);
//        when(applicationUseCase.ApplyForLoan(any(Application.class)))
//                .thenReturn(Mono.error(new IllegalArgumentException("Invalid application")));
//
//        // Act & Assert
//        StepVerifier.create(handler.submitApplicationUseCase(serverRequest))
//                .expectError(IllegalArgumentException.class)
//                .verify();
//
//        verify(loanApplicationDTOMapper).toModel(any(ApplicationRequestDTO.class));
//        verify(applicationUseCase).ApplyForLoan(application);
//    }
//
//    @Test
//    void listApplicationsUseCase_WhenValidRequest_ShouldReturnOk() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.get("/applications?page=0&size=10&status=pending")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
//                .build();
//
//        ServerRequest serverRequest = ServerRequest.create(
//                MockServerWebExchange.from(request),
//                Collections.emptyList()
//        );
//
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                "user", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//        SecurityContext securityContext = new SecurityContextImpl(authentication);
//
//        ApplicationListResponse response = ApplicationListResponse.builder()
//                .applications(Collections.emptyList())
//                .currentPage(0)
//                .totalPages(1)
//                .totalElements(0L)
//                .build();
//
//        when(applicationListUseCase.listApplications(anyInt(), anyInt(), anyString(), anyString()))
//                .thenReturn(Mono.just(response));
//
//        // Act & Assert - Forma correcta usando subscriberContext (para versiones anteriores) o contextWrite
//        StepVerifier.create(
//                        handler.listApplicationsUseCase(serverRequest)
//                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
//                )
//                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.OK)
//                .verifyComplete();
//
//        verify(applicationListUseCase).listApplications(0, 10, "pending", "Bearer token123");
//    }
//
//    @Test
//    void listApplicationsUseCase_WhenNoStatusParam_ShouldReturnOk() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.get("/applications?page=0&size=10")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
//                .build();
//
//        ServerRequest serverRequest = ServerRequest.create(
//                MockServerWebExchange.from(request),
//                Collections.emptyList()
//        );
//
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                "user", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//        SecurityContext securityContext = new SecurityContextImpl(authentication);
//
//        ApplicationListResponse response = ApplicationListResponse.builder()
//                .applications(Collections.emptyList())
//                .currentPage(0)
//                .totalPages(1)
//                .totalElements(0L)
//                .build();
//
//        when(applicationListUseCase.listApplications(anyInt(), anyInt(), any(), anyString()))
//                .thenReturn(Mono.just(response));
//
//        // Act & Assert
//        StepVerifier.create(
//                        handler.listApplicationsUseCase(serverRequest)
//                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
//                )
//                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.OK)
//                .verifyComplete();
//
//        verify(applicationListUseCase).listApplications(0, 10, null, "Bearer token123");
//    }
//
//    @Test
//    void listApplicationsUseCase_WhenUseCaseThrowsError_ShouldPropagateError() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.get("/applications?page=0&size=10")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
//                .build();
//
//        ServerRequest serverRequest = ServerRequest.create(
//                MockServerWebExchange.from(request),
//                Collections.emptyList()
//        );
//
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                "user", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//        SecurityContext securityContext = new SecurityContextImpl(authentication);
//
//        when(applicationListUseCase.listApplications(anyInt(), anyInt(), any(), anyString()))
//                .thenReturn(Mono.error(new RuntimeException("Database error")));
//
//        // Act & Assert
//        StepVerifier.create(
//                        handler.listApplicationsUseCase(serverRequest)
//                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
//                )
//                .expectError(RuntimeException.class)
//                .verify();
//
//        verify(applicationListUseCase).listApplications(0, 10, null, "Bearer token123");
//    }
//
//    @Test
//    void listApplicationsUseCase_WhenNoAuthHeader_ShouldHandleGracefully() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.get("/applications?page=0&size=10")
//                .build();
//
//        ServerRequest serverRequest = ServerRequest.create(
//                MockServerWebExchange.from(request),
//                Collections.emptyList()
//        );
//
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                "user", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//        SecurityContext securityContext = new SecurityContextImpl(authentication);
//
//        ApplicationListResponse response = ApplicationListResponse.builder()
//                .applications(Collections.emptyList())
//                .currentPage(0)
//                .totalPages(1)
//                .totalElements(0L)
//                .build();
//
//        when(applicationListUseCase.listApplications(anyInt(), anyInt(), any(), any()))
//                .thenReturn(Mono.just(response));
//
//        // Act & Assert
//        StepVerifier.create(
//                        handler.listApplicationsUseCase(serverRequest)
//                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
//                )
//                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.OK)
//                .verifyComplete();
//
//        verify(applicationListUseCase).listApplications(0, 10, null, null);
//    }
//
//    @Test
//    void listApplicationsUseCase_WhenNoSecurityContext_ShouldHandleGracefully() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.get("/applications?page=0&size=10")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
//                .build();
//
//        ServerRequest serverRequest = ServerRequest.create(
//                MockServerWebExchange.from(request),
//                Collections.emptyList()
//        );
//
//        ApplicationListResponse response = ApplicationListResponse.builder()
//                .applications(Collections.emptyList())
//                .currentPage(0)
//                .totalPages(1)
//                .totalElements(0L)
//                .build();
//
//        when(applicationListUseCase.listApplications(anyInt(), anyInt(), any(), anyString()))
//                .thenReturn(Mono.just(response));
//
//        // Act & Assert - Sin contexto de seguridad
//        StepVerifier.create(handler.listApplicationsUseCase(serverRequest))
//                .expectNextMatches(serverResponse -> serverResponse.statusCode() == HttpStatus.OK)
//                .verifyComplete();
//
//        verify(applicationListUseCase).listApplications(0, 10, null, "Bearer token123");
//    }
}