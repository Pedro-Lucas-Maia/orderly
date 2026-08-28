package bti.pds.dinner.auth.infrastructure.http.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.NonNull;

public record ResetPasswordRequest(
        @NotBlank String token,

        @NotBlank
        @Length(min = 8,  max = 20, message = "New password must be between 8 and 20 characters")
        String newPassword
) {
    public static bti.pds.dinner.auth.application.input.ConfirmPasswordResetInput toInput(@NonNull ResetPasswordRequest request) {
        return new bti.pds.dinner.auth.application.input.ConfirmPasswordResetInput(request.token(), request.newPassword());
    }
}
