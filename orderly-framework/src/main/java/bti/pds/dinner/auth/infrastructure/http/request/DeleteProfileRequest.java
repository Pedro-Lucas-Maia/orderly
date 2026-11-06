package bti.pds.dinner.auth.infrastructure.http.request;

import bti.pds.dinner.auth.application.input.DeleteProfileInput;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;

public record DeleteProfileRequest(
        @NotBlank(message = "Password is required")
        String password
) {
    public static DeleteProfileInput toInput(@NonNull DeleteProfileRequest request, String email) {
        return new DeleteProfileInput(request.password(), email);
    }
}
