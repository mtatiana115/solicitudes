package co.com.bancolombia.usecase.application;

import co.com.bancolombia.model.application.Application;
import co.com.bancolombia.model.application.dto.ApplicationList;
import co.com.bancolombia.model.application.dto.ApplicationListResponse;
import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.auth.User;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationListUseCaseTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private IUserRestConsumer userRestConsumer;

    @InjectMocks
    private ApplicationListUseCase applicationListUseCase;

    private Application validApplication;
    private LoanType validLoanType;

    @BeforeEach
    void setUp() {
        validApplication = Application.builder()
                .id("1")
                .amount(new BigDecimal("100000"))
                .term(12)
                .email("user@example.com")
                .loanTypeId(1)
                .statusId(1)
                .documentId("123456")
                .build();

        validLoanType = LoanType.builder()
                .Id(1)
                .name("Personal Loan")
                .minAmount(new BigDecimal("50000"))
                .maxAmount(new BigDecimal("1000000"))
                .interestRate(new BigDecimal("0.01"))
                .automaticValidation(true)
                .build();
    }

    @Test
    void listApplications_WhenValidInput_ShouldReturnApplicationListResponse() {

        User user = new User("ADMIN", "user@example.com", "123456", "John Doe", new BigDecimal("5000"));
        when(applicationRepository.countFilteredApplications(null, 1)).thenReturn(Mono.just(1L));
        when(applicationRepository.filterApplications(0, 10, null, 1)).thenReturn(Flux.just(validApplication));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(validLoanType));
        when(userRestConsumer.findUserByEmail("user@example.com", "token")).thenReturn(Mono.just(user));

        Mono<ApplicationListResponse> result = applicationListUseCase.listApplications(0, 10, "pending review", "token");

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(applicationRepository).countFilteredApplications(null, 1);
        verify(applicationRepository).filterApplications(0, 10, null, 1);
        verify(loanTypeRepository).findById(1);
        verify(userRestConsumer).findUserByEmail("user@example.com", "token");
    }

    @Test
    void listApplications_WhenUserServiceFails_ShouldReturnDefaultUser() {

        when(applicationRepository.countFilteredApplications(null, 1)).thenReturn(Mono.just(1L));
        when(applicationRepository.filterApplications(0, 10, null, 1)).thenReturn(Flux.just(validApplication));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(validLoanType));
        when(userRestConsumer.findUserByEmail("user@example.com", "token"))
                .thenReturn(Mono.error(new RuntimeException("User service down")));

        Mono<ApplicationListResponse> result = applicationListUseCase.listApplications(0, 10, "pending review", "token");

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    List<ApplicationList> applications = response.getApplications();
                    return applications.size() == 1 &&
                            "1".equals(applications.get(0).getId()) &&
                            applications.get(0).getUserName().equals("Unknown User") &&
                            applications.get(0).getBaseSalary().equals(BigDecimal.ZERO);
                })
                .verifyComplete();

        verify(applicationRepository).countFilteredApplications(null, 1);
        verify(applicationRepository).filterApplications(0, 10, null, 1);
        verify(loanTypeRepository).findById(1);
        verify(userRestConsumer).findUserByEmail("user@example.com", "token");
    }

    @Test
    void listApplications_WhenNegativePageAndSize_ShouldUseDefaults() {

        User user = new User("ADMIN", "user@example.com", "123456", "John Doe", new BigDecimal("5000"));
        when(applicationRepository.countFilteredApplications(null, 1)).thenReturn(Mono.just(1L));
        when(applicationRepository.filterApplications(0, 1, null, 1)).thenReturn(Flux.just(validApplication));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.just(validLoanType));
        when(userRestConsumer.findUserByEmail("user@example.com", "token")).thenReturn(Mono.just(user));

        Mono<ApplicationListResponse> result = applicationListUseCase.listApplications(-1, -5, "pending review", "token");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getCurrentPage() == -1 &&
                        response.getApplications().size() == 1 &&
                        response.getApplications().get(0).getId().equals("1"))
                .verifyComplete();

        verify(applicationRepository).countFilteredApplications(null, 1);
        verify(applicationRepository).filterApplications(0, 1, null, 1);
        verify(loanTypeRepository).findById(1);
        verify(userRestConsumer).findUserByEmail("user@example.com", "token");
    }

    @Test
    void listApplications_WhenLoanTypeRepositoryThrowsError_ShouldPropagateError() {

        when(applicationRepository.countFilteredApplications(null, 1)).thenReturn(Mono.just(1L));
        when(applicationRepository.filterApplications(0, 10, null, 1)).thenReturn(Flux.just(validApplication));
        when(loanTypeRepository.findById(1)).thenReturn(Mono.error(new RuntimeException("DB is down")));

        Mono<ApplicationListResponse> result = applicationListUseCase.listApplications(0, 10, "pending review", "token");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(applicationRepository).countFilteredApplications(null, 1);
        verify(applicationRepository).filterApplications(0, 10, null, 1);
        verify(loanTypeRepository).findById(1);
    }

    @Test
    void listApplications_WhenApplicationRepositoryCountThrowsError_ShouldPropagateError() {
        when(applicationRepository.countFilteredApplications(null, 1))
                .thenReturn(Mono.error(new RuntimeException("DB is down")));
        when(applicationRepository.filterApplications(0, 10, null, 1))
                .thenReturn(Flux.just(validApplication));

        Mono<ApplicationListResponse> result = applicationListUseCase.listApplications(0, 10, "pending review", "token");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(applicationRepository).countFilteredApplications(null, 1);
        verify(applicationRepository).filterApplications(0, 10, null, 1);
        verify(loanTypeRepository, never()).findById(anyInt());
        verify(userRestConsumer, never()).findUserByEmail(anyString(), anyString());
    }

    @Test
    void listApplications_WhenApplicationRepositoryFilterThrowsError_ShouldPropagateError() {
        when(applicationRepository.countFilteredApplications(null, 1)).thenReturn(Mono.just(1L));
        when(applicationRepository.filterApplications(0, 10, null, 1))
                .thenReturn(Flux.error(new RuntimeException("DB is down")));

        Mono<ApplicationListResponse> result = applicationListUseCase.listApplications(0, 10, "pending review", "token");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(applicationRepository).countFilteredApplications(null, 1);
        verify(applicationRepository).filterApplications(0, 10, null, 1);
        verify(loanTypeRepository, never()).findById(anyInt());
        verify(userRestConsumer, never()).findUserByEmail(anyString(), anyString());
    }
}