package bti.pds.dinner.auth.infrastructure.http.request;

import bti.pds.dinner.auth.application.input.RegisterInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.NonNull;

public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Length(min = 2,  max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Length(min = 8,  max = 20, message = "Password must be between 8 and 20 characters")
        String password
) {
    public static RegisterInput toInput(@NonNull RegisterRequest registerRequest) {
        return new RegisterInput(
                registerRequest.name(),
                registerRequest.email(),
                registerRequest.password()
        );
    }
}
