package bti.pds.dinner.store.infrastructure.http.request;

import bti.pds.dinner.store.application.input.UpdateStoreStatusInput;
import bti.pds.dinner.store.domain.StoreStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStoreStatusRequest(
        @NotNull(message = "Status is required")
        StoreStatus status
) {
    public static UpdateStoreStatusInput toInput(UpdateStoreStatusRequest request) {
        return new UpdateStoreStatusInput(request.status());
    }
}
