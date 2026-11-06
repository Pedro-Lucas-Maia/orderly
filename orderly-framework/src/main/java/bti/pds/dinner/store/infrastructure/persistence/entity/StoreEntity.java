package bti.pds.dinner.store.infrastructure.persistence.entity;

import bti.pds.dinner.store.domain.Store;
import bti.pds.dinner.store.domain.StoreStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

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

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "address_number")
    private String addressNumber;

    @Column(name = "address_neighborhood")
    private String addressNeighborhood;

    @Column(name = "address_city")
    private String addressCity;

    @Column(name = "address_zip_code")
    private String addressZipCode;

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
                .addressStreet(store.getAddressStreet())
                .addressNumber(store.getAddressNumber())
                .addressNeighborhood(store.getAddressNeighborhood())
                .addressCity(store.getAddressCity())
                .addressZipCode(store.getAddressZipCode())
                .build();
    }

    public static Store toDomain(@NonNull StoreEntity entity) {
        return new Store(
                entity.getId(),
                entity.getName(),
                entity.getOpeningTime(),
                entity.getClosingTime(),
                entity.getMaxOrdersInProgress(),
                entity.isAutomaticPause(),
                entity.getStatus(),
                entity.getManualStatus(),
                entity.getAddressStreet(),
                entity.getAddressNumber(),
                entity.getAddressNeighborhood(),
                entity.getAddressCity(),
                entity.getAddressZipCode()
        );
    }
}
