package bti.pds.dinner.sales.domain;

import org.springframework.stereotype.Component;

@Component
public class BurgerSaleConsumptionStrategy implements SaleConsumptionStrategy {

    @Override
    public boolean canBeCancelled(SaleStatus saleStatus) {
        return saleStatus == SaleStatus.PENDENTE;
    }

    @Override
    public boolean shouldRestoreOnCancellation(SaleStatus saleStatus) {
        return saleStatus == SaleStatus.PENDENTE;
    }

    @Override
    public double getProductionLossFactor() {
        return 0.0;
    }
}
