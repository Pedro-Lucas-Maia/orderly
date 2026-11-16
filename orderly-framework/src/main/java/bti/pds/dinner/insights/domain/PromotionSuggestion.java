package bti.pds.dinner.insights.domain;

import java.time.LocalDate;
import java.util.List;

public record PromotionSuggestion(
        Long stockItemId,
        Long lotId,
        LocalDate expiresAt,
        int quantityInLot,
        List<SuggestedProduct> suggestedProducts
) implements InventoryAction {
}
