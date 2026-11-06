package bti.pds.dinner.auth.infrastructure.http.response;

public record AuthResponse(String id, String name, String email, String role, String cookie) {
}
