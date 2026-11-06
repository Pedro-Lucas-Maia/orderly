package bti.pds.dinner.store.domain.exception;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException() {
        super("Default Store not found");
    }
}
