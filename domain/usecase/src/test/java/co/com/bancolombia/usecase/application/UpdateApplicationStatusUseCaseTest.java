package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.auxmodels.DecisionEvent;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.auth.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateApplicationStatusUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private IUserRestConsumer userRestConsumer;

    @InjectMocks
    private UpdateApplicationStatusUseCase useCase;

    private Application application;
    private User user;

    @BeforeEach
    void setUp() {
        application = Application.builder()
                .id(UUID.randomUUID().toString())
                .amount(new BigDecimal("5000000"))
                .term(24)
                .email("test@example.com")
                .statusId(1)
                .loanTypeId(1)
                .build();

        user = new User("ADVISOR", "test@example.com", "123456789", "Test User", new BigDecimal("50000"));
    }

//    @Test
//    @DisplayName("Debería actualizar el estado y el documentId del usuario")
//    void shouldUpdateStatusAndDocumentId() {
//        // Configuración de los mocks para un flujo exitoso
//        when(applicationRepository.findByEmailAndId(anyString(), anyString())).thenReturn(Mono.just(application));
//        when(userRestConsumer.findUserByEmail(anyString())).thenReturn(Mono.just(user));
//        when(applicationRepository.save(any(Application.class))).thenReturn(Mono.just(application));
//
//        // Datos de entrada con el nuevo estado
//        Application updatedApplication = Application.builder().id(application.getId()).email(application.getEmail()).statusId(4).build();
//
//        // Ejecución y verificación del caso de uso
//        StepVerifier.create(useCase.updateApplicationStatus(updatedApplication))
//                .expectNextMatches(savedApp -> savedApp.getStatusId().equals(4) && savedApp.getDocumentId().equals(user.documentId()))
//                .verifyComplete();
//
//        // Verificación de llamadas a mocks
//        verify(applicationRepository).save(any(Application.class));
//    }

    @Test
    @DisplayName("Debería lanzar una excepción si el estado es el mismo")
    void shouldThrowExceptionIfStatusIsTheSame() {

        when(applicationRepository.findByEmailAndId(anyString(), anyString())).thenReturn(Mono.just(application));
        when(userRestConsumer.findUserByEmail(anyString())).thenReturn(Mono.just(user));

        Application sameStatusApplication = Application.builder().id(application.getId()).email(application.getEmail()).statusId(1).build();

        StepVerifier.create(useCase.updateApplicationStatus(sameStatusApplication))
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException &&
                        throwable.getMessage().contains("La solicitud ya está en estado pending review"))
                .verify();

        verify(applicationRepository, never()).save(any(Application.class));
    }
}
