package co.com.bancolombia.api;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> Mono<T> validate(T dto, String defaultMessage) {
        return Mono.fromCallable(() -> {
            var errors = new BeanPropertyBindingResult(dto, dto.getClass().getName());
            validator.validate(dto, errors);
            if (errors.hasErrors()) {
                String detail = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .reduce((a, b) -> a + "; " + b)
                        .orElse(defaultMessage);
                throw new ValidationException(detail);
            }
            return dto;
        });
    }
}
