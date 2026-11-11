package bti.pds.dinner.sales.domain.exception;

import bti.pds.dinner.common.exception.ConflictException;

public class StoreUnavailableException extends ConflictException {
    public StoreUnavailableException() {
        super("Store unavailable", "Store is not available for orders");
    }
}
