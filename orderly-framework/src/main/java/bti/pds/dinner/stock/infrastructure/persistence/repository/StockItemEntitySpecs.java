package bti.pds.dinner.stock.infrastructure.persistence.repository;

import bti.pds.dinner.stock.infrastructure.persistence.entity.StockItemEntity;
import org.springframework.data.jpa.domain.PredicateSpecification;

public class StockItemEntitySpecs {
    static PredicateSpecification<StockItemEntity> belongsToStock(Long stockId) {
        return (from, builder) -> {
            if (stockId == null) return null;
            return builder.equal(from.get("stockId"), stockId);
        };
    }

    static PredicateSpecification<StockItemEntity> isNotDeleted() {
        return (from, builder) -> builder.isNull(from.get("deletedAt"));
    }
    static PredicateSpecification<StockItemEntity> hasCategory(String category) {
        return (from, builder) -> {
            if (category == null || category.isBlank()) return null;
            return builder.equal(from.get("category"), category);
        };
    }
    static PredicateSpecification<StockItemEntity> hasName(String name) {
        return (from, builder) -> {
            if (name == null || name.isBlank()) return null;
            return builder.like(builder.lower(from.get("name")), '%' + name.toLowerCase() + '%');
        };
    }
    static PredicateSpecification<StockItemEntity> hasActive(Boolean active) {
        return (from, builder) -> {
            if (active == null) return null;
            return builder.equal(from.get("active"), active);
        };
    }
}
