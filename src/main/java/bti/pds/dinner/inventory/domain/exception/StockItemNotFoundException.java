package bti.pds.dinner.inventory.domain.exception;

public class StockItemNotFoundException extends RuntimeException {
    public StockItemNotFoundException(String message) {
        super(message);
    }
}
