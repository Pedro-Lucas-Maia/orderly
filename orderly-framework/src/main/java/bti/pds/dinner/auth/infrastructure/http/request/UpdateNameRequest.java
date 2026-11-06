package bti.pds.dinner.auth.infrastructure.http.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.jspecify.annotations.NonNull;

public record UpdateNameRequest(
        @NotBlank(message = "Name is required")
        @Length(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name
) {
    public static bti.pds.dinner.auth.application.input.UpdateNameInput toInput(@NonNull UpdateNameRequest request, String email) {
        return new bti.pds.dinner.auth.application.input.UpdateNameInput(request.name(), email);
    }
}