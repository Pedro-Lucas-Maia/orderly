package bti.pds.dinner.insights.domain;

public record SuggestedProduct(
        Long productId,
        String productName,
        int predictedQuantityToday
) {
}
