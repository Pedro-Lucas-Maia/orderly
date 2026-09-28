package bti.pds.dinner.sales.domain;

public interface StoreAvailability {
    boolean isAvailableForOrders(Long storeId);
}
