package co.com.bancolombia.model.auth;

public record User(
        String rolName,
        String email,
        String documentId

) {
}
