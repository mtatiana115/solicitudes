package co.com.bancolombia.model.application.gateways;

public interface LoggerRepository {
    void info(String message, Object... args);
    void warn(String message, Object args);
    void error(String message, Object... args);
}