package bti.pds.dinner.sales.domain.exception;

public class StoreUnavailableException extends SaleException {
    public StoreUnavailableException(Long storeId) {
        super("Store is not available for orders: " + storeId);
    }
}
