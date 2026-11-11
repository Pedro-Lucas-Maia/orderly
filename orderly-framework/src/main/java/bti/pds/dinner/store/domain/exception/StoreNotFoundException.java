package bti.pds.dinner.store.domain.exception;

import bti.pds.dinner.common.exception.ResourceNotFoundException;

public class StoreNotFoundException extends ResourceNotFoundException {
    public StoreNotFoundException() {
        super("Store not found", "Default Store not found");
    }
}
