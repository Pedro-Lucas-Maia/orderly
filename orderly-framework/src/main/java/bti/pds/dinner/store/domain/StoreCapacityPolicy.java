package bti.pds.dinner.store.domain;

public interface StoreCapacityPolicy {
    boolean canAcceptOrder(Store store, int currentOrdersInProgress);
}
