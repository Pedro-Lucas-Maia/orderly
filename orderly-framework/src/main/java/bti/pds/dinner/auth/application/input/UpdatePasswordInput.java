package bti.pds.dinner.auth.application.input;

public record UpdatePasswordInput(
        String currentPassword,
        String newPassword,
        String email
) {
}
