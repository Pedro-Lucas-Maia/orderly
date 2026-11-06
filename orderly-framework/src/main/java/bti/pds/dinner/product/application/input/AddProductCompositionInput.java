package bti.pds.dinner.product.application.input;

public record AddProductCompositionInput(
        Long stockItemId,
        int quantity
) {
}
