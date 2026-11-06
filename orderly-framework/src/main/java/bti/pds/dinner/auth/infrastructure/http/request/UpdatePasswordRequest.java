package bti.pds.dinner.auth.infrastructure.http.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.NonNull;

public record UpdatePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Length(min = 8, max = 20, message = "New password must be between 8 and 20 characters long")
        String newPassword
) {
    public static bti.pds.dinner.auth.application.input.UpdatePasswordInput toInput(@NonNull UpdatePasswordRequest request, String email) {
        return new bti.pds.dinner.auth.application.input.UpdatePasswordInput(request.currentPassword(), request.newPassword(), email);
    }
}