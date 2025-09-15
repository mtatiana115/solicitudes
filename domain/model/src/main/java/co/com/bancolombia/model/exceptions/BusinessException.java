package co.com.bancolombia.model.exceptions;

public class BusinessException extends RuntimeException{
    private final ErrorType errorType;

    public BusinessException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public enum ErrorType {
        NOT_FOUND,
        INVALID_STATE_TRANSITION
    }
}
