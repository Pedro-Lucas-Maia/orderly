package bti.pds.dinner.sales.domain;

public record RecipeItem(
    Long stockItemId,
    int quantityPerUnit
) {}
