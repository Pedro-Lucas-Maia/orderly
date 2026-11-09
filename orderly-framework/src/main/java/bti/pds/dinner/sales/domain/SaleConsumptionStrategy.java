package bti.pds.dinner.sales.domain;

public interface SaleConsumptionStrategy {
    boolean canBeCancelled(SaleStatus saleStatus);
    boolean shouldRestoreOnCancellation(SaleStatus saleStatus);

    default double getProductionLossFactor() {
        return 0.0;
    }
}
