package bti.pds.dinner.sales.domain.exception;

public class InvalidSaleStateException extends SaleException {
    public InvalidSaleStateException(String message) {
        super(message);
    }
}
