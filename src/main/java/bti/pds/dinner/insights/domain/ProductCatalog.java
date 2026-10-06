package bti.pds.dinner.insights.domain;

import java.util.List;

public interface ProductCatalog {
    List<CatalogProduct> findActiveProducts();
}
