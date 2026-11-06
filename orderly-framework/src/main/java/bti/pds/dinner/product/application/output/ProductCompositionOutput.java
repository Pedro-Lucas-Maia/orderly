package bti.pds.dinner.product.application.output;

public record ProductCompositionOutput(
        Long id,
        Long productId,
        Long stockItemId,
        int quantity
) {
}
