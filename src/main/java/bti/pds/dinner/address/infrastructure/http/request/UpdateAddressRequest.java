package bti.pds.dinner.address.infrastructure.http.request;

import bti.pds.dinner.address.application.input.UpdateAddressInput;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record UpdateAddressRequest(
        @NotBlank(message = "Address name is required")
        String name,

        @NotBlank(message = "Address street is required")
        String street,

        @Digits(message = "Address number must be digits", integer = 20, fraction = 0)
        String number,

        @NotBlank(message = "Address complement is required")
        String complement,

        @NotBlank(message = "Address neigh is required")
        String neighborhood,
        String city,
        String state,
        String zipCode
) {
    public static UpdateAddressInput toInput(@NonNull UpdateAddressRequest request, String addressId, String userId) {
        return new UpdateAddressInput(
                UUID.fromString(addressId),
                UUID.fromString(userId),
                request.name(),
                request.street(),
                request.number(),
                request.complement(),
                request.neighborhood(),
                request.city(),
                request.state(),
                request.zipCode()
        );
    }
}
