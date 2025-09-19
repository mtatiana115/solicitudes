package co.com.bancolombia.config;

import co.com.bancolombia.model.application.gateways.ApplicationRepository;
import co.com.bancolombia.model.application.gateways.DecisionEventSenderRepository;
import co.com.bancolombia.model.application.gateways.IUserRestConsumer;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.usecase.application.ApplicationListUseCase;
import co.com.bancolombia.usecase.application.ApplicationUseCase;
import co.com.bancolombia.usecase.application.UpdateApplicationStatusUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "co.com.bancolombia.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {
}
