package bti.pds.dinner.auth.application.input;

public record DeleteProfileInput(
        String password,
        String email
) {
}
