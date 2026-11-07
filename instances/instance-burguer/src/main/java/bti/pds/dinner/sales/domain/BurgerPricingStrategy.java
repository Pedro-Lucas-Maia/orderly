package bti.pds.dinner.sales.domain;

import bti.pds.dinner.sales.domain.exception.OutOfDeliveryAreaException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BurgerPricingStrategy implements OrderPricingStrategy {
    @Override
    public BigDecimal calculateOrderTotal(Sale sale, @NonNull List<SaleItem> items, BigDecimal baseDeliveryFee) {
        var subTotal = items.stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (baseDeliveryFee == null) {
            return subTotal;
        }
        return subTotal.add(baseDeliveryFee);
    }

    @Override
    public BigDecimal calculateDeliveryFee(Address address) {
        if (address.getCity() == null || !address.getCity().trim().equalsIgnoreCase("Natal")) {
            throw new OutOfDeliveryAreaException("Unfortunately, we do not deliver to the city: " + address.getCity());
        }

        String normalizedNeighborhood = address.getNeighborhood() != null ? address.getNeighborhood().trim().toLowerCase() : "";

        return switch (normalizedNeighborhood) {
            case "ponta negra", "capim macio", "neópolis" -> new BigDecimal("5.00");
            case "candelária", "lagoa nova", "nova descoberta", "tirol", "petrópolis" -> new BigDecimal("7.50");
            case "alecrim", "quintas", "bairro das roças", "areia preta", "mãe luiza" -> new BigDecimal("10.00");
            case "zona norte", "potengi", "pajuçara", "redinha", "igapó" -> new BigDecimal("15.00");
            default -> new BigDecimal("10.00"); // Valor padrão para bairros não listados
        };
    }
}
