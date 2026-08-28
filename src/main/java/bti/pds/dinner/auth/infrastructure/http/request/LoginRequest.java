package bti.pds.dinner.auth.infrastructure.http.request;

import bti.pds.dinner.auth.application.input.LoginInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.NonNull;

public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Length(min = 8,  max = 20)
        String password) {
    public static LoginInput toInput(@NonNull LoginRequest loginRequest) {
        return new LoginInput(
                loginRequest.email(),
                loginRequest.password()
        );
    }
}
