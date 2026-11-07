package bti.pds.dinner.sales.domain;

import java.math.BigDecimal;
import java.util.List;

public interface OrderPricingStrategy {
    BigDecimal calculateOrderTotal(Sale sale, List<SaleItem> items, BigDecimal baseDeliveryFee);
    BigDecimal calculateDeliveryFee(Address address);
}
