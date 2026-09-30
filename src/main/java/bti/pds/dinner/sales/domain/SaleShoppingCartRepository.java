package bti.pds.dinner.sales.domain;

import bti.pds.dinner.sales.application.input.SaleItemInput;

import java.util.List;

public interface SaleShoppingCartRepository {
    List<SaleItemInput> getItems(String shoppingCartId);
    void clearItems(String shoppingCartId);
}
