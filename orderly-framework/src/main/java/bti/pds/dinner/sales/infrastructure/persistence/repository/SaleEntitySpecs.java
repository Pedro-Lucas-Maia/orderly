package bti.pds.dinner.sales.infrastructure.persistence.repository;

import bti.pds.dinner.sales.domain.SaleStatus;
import bti.pds.dinner.sales.infrastructure.persistence.entity.SaleEntity;
import org.springframework.data.jpa.domain.PredicateSpecification;

import java.time.LocalDateTime;

public class SaleEntitySpecs {
    static PredicateSpecification<SaleEntity> hasDate(LocalDateTime date) {
        return (from, builder) -> {
            if (date == null) return null;

            return builder.equal(from.get("date"), date);
        };
    }

    static PredicateSpecification<SaleEntity> hasStatus(SaleStatus status) {
        return (from, builder) -> {
            if (status == null) return null;
            return builder.equal(from.get("status"), status);
        };
    }
}
