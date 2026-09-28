package bti.pds.dinner.store.infrastructure.persistence.entity;

import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.StoreStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "stores")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @Column(name = "max_orders_in_progress", nullable = false)
    private int maxOrdersInProgress;

    @Column(name = "automatic_pause", nullable = false)
    private boolean automaticPause;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "manual_status")
    private StoreStatus manualStatus;

    public static StoreEntity from(Store store) {
        return StoreEntity.builder()
                .id(store.getId())
                .name(store.getName())
                .openingTime(store.getOpeningTime())
                .closingTime(store.getClosingTime())
                .maxOrdersInProgress(store.getMaxOrdersInProgress())
                .automaticPause(store.isAutomaticPause())
                .status(store.getStatus())
                .manualStatus(store.getManualStatus())
                .build();
    }

    public static Store toDomain(StoreEntity entity) {
        return new Store(
                entity.id,
                entity.name,
                entity.openingTime,
                entity.closingTime,
                entity.maxOrdersInProgress,
                entity.automaticPause,
                entity.status,
                entity.manualStatus
        );
    }
}
