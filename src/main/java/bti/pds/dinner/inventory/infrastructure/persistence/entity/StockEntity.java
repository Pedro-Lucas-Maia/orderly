package bti.pds.dinner.inventory.infrastructure.persistence.entity;

import bti.pds.dinner.inventory.domain.Stock;
import bti.pds.dinner.inventory.domain.StockId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

@Entity
@Table(name = "stocks")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public static StockEntity from(@NonNull Stock stock) {
        return StockEntity.builder()
                .id(stock.getId() != null ? stock.getId().value() : null)
                .name(stock.getName())
                .build();
    }

    public static Stock toDomain(@NonNull StockEntity entity) {
        return Stock.builder()
                .id(new StockId(entity.getId()))
                .name(entity.getName())
                .build();
    }
}
