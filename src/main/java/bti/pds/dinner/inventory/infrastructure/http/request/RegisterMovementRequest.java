package bti.pds.dinner.inventory.infrastructure.http.request;

import bti.pds.dinner.inventory.application.input.RegisterMovementInput;
import bti.pds.dinner.inventory.domain.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

public record RegisterMovementRequest(
        @NotNull(message = "Type is required")
        MovementType type,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @NotBlank(message = "Reason is required")
        String reason
) {
    public static RegisterMovementInput toInput(@NonNull RegisterMovementRequest request) {
        return new RegisterMovementInput(
                request.type(),
                request.quantity(),
                request.reason()
        );
    }
}
