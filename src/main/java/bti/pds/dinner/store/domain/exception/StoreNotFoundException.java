package bti.pds.dinner.store.domain.exception;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(Long id) {
        super("Store not found: " + id);
    }
}
