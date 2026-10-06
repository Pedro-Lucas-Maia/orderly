package bti.pds.dinner.stock.domain.exception;

public class MissingLotExpiryException extends RuntimeException {
    public MissingLotExpiryException() {
        super("Expiry date is required for ENTRADA movements");
    }
}
