package bti.pds.dinner.insights.domain;

public record RecipeLine(
        Long productId,
        Long stockItemId,
        int quantityPerUnit
) {
}
