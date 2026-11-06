package bti.pds.dinner.auth.application.input;

public record ConfirmPasswordResetInput(
        String token,
        String newPassword
) {
}
