package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private IUserRestConsumer userRestConsumer;

    @InjectMocks
    private ApplicationUseCase applicationUseCase;

    private Application validApplication;

    @BeforeEach
    void setUp() {
        validApplication = Application.builder()
                .amount(BigDecimal.valueOf(10000000))
                .loanTypeId(1)
                .email("testuser@test.com")
                .build();
    }

    @Test
    void applyForLoan_WhenUserExistsAndLoanTypeIsValid_ShouldSaveApplication() {
        when(userRestConsumer.existsUserByEmail(validApplication.getEmail())).thenReturn(Mono.just(true));
        when(loanTypeRepository.findById(validApplication.getLoanTypeId())).thenReturn(Mono.just(new LoanType()));
        when(applicationRepository.save(any(Application.class))).thenReturn(Mono.just(validApplication));

        Mono<Application> result = applicationUseCase.ApplyForLoan(validApplication);

        StepVerifier.create(result)
                .expectNext(validApplication)
                .verifyComplete();

        verify(userRestConsumer).existsUserByEmail(validApplication.getEmail());
        verify(loanTypeRepository).findById(validApplication.getLoanTypeId());
        verify(applicationRepository).save(validApplication);
    }

    @Test
    void applyForLoan_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRestConsumer.existsUserByEmail(validApplication.getEmail())).thenReturn(Mono.just(false));

        Mono<Application> result = applicationUseCase.ApplyForLoan(validApplication);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("User already exists!"))
                .verify();

        verify(userRestConsumer).existsUserByEmail(validApplication.getEmail());
        verify(loanTypeRepository, never()).findById(anyInt());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyForLoan_WhenLoanTypeDoesNotExist_ShouldThrowException() {
        when(userRestConsumer.existsUserByEmail(validApplication.getEmail())).thenReturn(Mono.just(true));
        when(loanTypeRepository.findById(validApplication.getLoanTypeId())).thenReturn(Mono.empty());

        Mono<Application> result = applicationUseCase.ApplyForLoan(validApplication);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("loan type with id: " + validApplication.getLoanTypeId() + " does not exist"))
                .verify();

        verify(userRestConsumer).existsUserByEmail(validApplication.getEmail());
        verify(loanTypeRepository).findById(validApplication.getLoanTypeId());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyForLoan_WhenConsumerThrowsError_ShouldPropagateError() {
        when(userRestConsumer.existsUserByEmail(any())).thenReturn(Mono.error(new RuntimeException("API is down")));

        Mono<Application> result = applicationUseCase.ApplyForLoan(validApplication);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(userRestConsumer).existsUserByEmail(validApplication.getEmail());
        verify(loanTypeRepository, never()).findById(anyInt());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyForLoan_WhenLoanTypeRepositoryThrowsError_ShouldPropagateError() {
        when(userRestConsumer.existsUserByEmail(any())).thenReturn(Mono.just(true));
        when(loanTypeRepository.findById(anyInt())).thenReturn(Mono.error(new RuntimeException("DB is down")));

        Mono<Application> result = applicationUseCase.ApplyForLoan(validApplication);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(userRestConsumer).existsUserByEmail(validApplication.getEmail());
        verify(loanTypeRepository).findById(validApplication.getLoanTypeId());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void applyForLoan_WhenApplicationRepositoryThrowsError_ShouldPropagateError() {
        when(userRestConsumer.existsUserByEmail(any())).thenReturn(Mono.just(true));
        when(loanTypeRepository.findById(anyInt())).thenReturn(Mono.just(new LoanType()));
        when(applicationRepository.save(any())).thenReturn(Mono.error(new RuntimeException("DB is down")));

        Mono<Application> result = applicationUseCase.ApplyForLoan(validApplication);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(userRestConsumer).existsUserByEmail(validApplication.getEmail());
        verify(loanTypeRepository).findById(validApplication.getLoanTypeId());
        verify(applicationRepository).save(any());
    }
}