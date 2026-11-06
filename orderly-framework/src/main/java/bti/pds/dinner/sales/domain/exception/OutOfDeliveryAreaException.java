package bti.pds.dinner.sales.domain.exception;

public class OutOfDeliveryAreaException extends RuntimeException {
    public OutOfDeliveryAreaException(String message) {
        super(message);
    }
}
