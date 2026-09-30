package bti.pds.dinner.address.infrastructure.http.request;

import bti.pds.dinner.address.application.input.CreateAddressInput;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateAddressRequest(
        @NotBlank(message = "Address name is required")
        String name,

        @NotBlank(message = "Address street is required")
        String street,

        @Digits(message = "Address number must be a digit", integer = 20, fraction = 0)
        String number,

        String complement,

        @NotBlank(message = "Address neighborhood is required")
        String neighborhood,

        @NotBlank(message = "Address city is required")
        String city,

        @NotBlank(message = "Address state is required")
        String state,

        @NotBlank(message = "Address zip code is required")
        String zipCode
) {
    public static CreateAddressInput toInput(CreateAddressRequest request, String userId) {
        return new CreateAddressInput(
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
