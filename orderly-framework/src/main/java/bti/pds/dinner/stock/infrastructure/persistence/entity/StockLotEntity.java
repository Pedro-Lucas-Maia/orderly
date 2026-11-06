package bti.pds.dinner.stock.infrastructure.persistence.entity;

import bti.pds.dinner.stock.domain.StockItemId;
import bti.pds.dinner.stock.domain.StockLot;
import bti.pds.dinner.stock.domain.StockLotId;
import jakarta.persistence.Column;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_lots")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockLotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_item_id")
    private Long stockItemId;

    private int quantity;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    public static StockLotEntity from(@NonNull StockLot lot) {
        return StockLotEntity.builder()
                .id(lot.getId() != null ? lot.getId().value() : null)
                .stockItemId(lot.getStockItemId().value())
                .quantity(lot.getQuantity())
                .expiresAt(lot.getExpiresAt())
                .receivedAt(lot.getReceivedAt())
                .build();
    }

    public static StockLot toDomain(@NonNull StockLotEntity entity) {
        return StockLot.builder()
                .id(new StockLotId(entity.getId()))
                .stockItemId(new StockItemId(entity.getStockItemId()))
                .quantity(entity.getQuantity())
                .expiresAt(entity.getExpiresAt())
                .receivedAt(entity.getReceivedAt())
                .build();
    }
}
