package bti.pds.dinner.sales.domain;

public record RecipeItem(
    String stockItemId,
    int quantityPerUnit
) {}
