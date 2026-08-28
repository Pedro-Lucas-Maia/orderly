package bti.pds.dinner.auth.application.input;

public record LoginInput(
        String email,
        String password
) {
}
