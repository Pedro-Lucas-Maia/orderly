package bti.pds.dinner.product.domain.exception;

public class ProductCompositionNotFoundException extends RuntimeException {
    public ProductCompositionNotFoundException(String message) {
        super(message);
    }
}
