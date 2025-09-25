package co.com.bancolombia.model.exceptions;

public class UnchangedStatusApplicationsException extends RuntimeException {
    public UnchangedStatusApplicationsException(String message) {
        super(message);
    }
}
