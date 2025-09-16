package co.com.bancolombia.api;

import co.com.bancolombia.model.exceptions.BusinessException;
import co.com.bancolombia.model.exceptions.ExternalServiceCommunicationException;
import co.com.bancolombia.model.exceptions.LoanTypeNotFoundException;
import io.r2dbc.spi.R2dbcException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalServiceCommunicationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleExternalService(ExternalServiceCommunicationException ex) {
        log.error("External service communication failed: service={}, endpoint={}, msg={}",
                ex.getService(), ex.getEndpoint(), ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("External Service Unavailable");
        problem.setDetail(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem));
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleValidationException(ValidationException ex) {
        log.warn("⚠ DTO validation error -> {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Bad Request");
        problem.setDetail(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Validation error -> {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Bad Request");
        problem.setDetail(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem));
    }

    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleDecodingException(DecodingException ex) {
        log.error("Request deserialization error -> {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Bad Request");
        problem.setDetail("Invalid request format");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleResponseStatusException(ResponseStatusException ex) {
        log.error("Response status exception -> {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(ex.getStatusCode());
        problem.setTitle(ex.getStatusCode().value() == 404 ? "Not Found" : "Error");
        problem.setDetail(ex.getStatusCode().value() == 404 ? "The requested resource does not exist" : "An unexpected error occurred");
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(problem));
    }

    @ExceptionHandler(R2dbcException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleR2dbcBadGrammarException(R2dbcException ex) {
        log.error("Database error occurred -> {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("A database error occurred");
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem));
    }

    @ExceptionHandler(ConnectException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleConnectException(ConnectException ex) {
        log.error("Database connection failed -> {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("A database connection error occurred");
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied -> {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Forbidden");
        problem.setDetail("Access denied: User does not have the required role");
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem));
    }

    @ExceptionHandler(LoanTypeNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleLoanTypeNotFound(LoanTypeNotFoundException ex) {
        log.warn("Loan type not found -> {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Not Found");
        problem.setDetail(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem));
    }

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleBusinessException(BusinessException ex) {
        log.warn("Error de negocio -> {}: {}", ex.getErrorType(), ex.getMessage());
        HttpStatus status = switch (ex.getErrorType()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_STATE_TRANSITION -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(status == HttpStatus.NOT_FOUND ? "Not Found" :
                status == HttpStatus.BAD_REQUEST ? "Bad Request" : "Internal Server Error");
        problem.setDetail(ex.getMessage());
        return Mono.just(ResponseEntity.status(status).body(problem));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred -> {}", ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred");
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem));
    }
}
