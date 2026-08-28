package bti.pds.dinner.auth.infrastructure.http.request;

import jakarta.validation.constraints.Email;
import org.jspecify.annotations.NonNull;

public record ForgotPasswordRequest(@Email String email) {
    public static bti.pds.dinner.auth.application.input.InitiatePasswordResetInput toInput(@NonNull ForgotPasswordRequest request) {
        return new bti.pds.dinner.auth.application.input.InitiatePasswordResetInput(request.email());
    }
}
