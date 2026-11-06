package bti.pds.dinner.address.infrastructure.http.request;

import bti.pds.dinner.address.application.input.AddressInput;

import java.util.UUID;

public record AddressRequest(
        @org.hibernate.validator.constraints.UUID(message = "Address id must be a valid UUID")
        String addressId,

        @org.hibernate.validator.constraints.UUID(message = "User id must be a valid UUID")
        String userId
) {
    public static AddressInput toInput(String addressId, String userId) {
        return new AddressInput(
                UUID.fromString(addressId),
                UUID.fromString(userId)
        );
    }
}
