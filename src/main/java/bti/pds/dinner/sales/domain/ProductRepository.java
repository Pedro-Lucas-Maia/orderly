package bti.pds.dinner.sales.domain;

import java.math.BigDecimal;

public interface ProductRepository {
    BigDecimal getCurrentPrice(String productId); 
} 