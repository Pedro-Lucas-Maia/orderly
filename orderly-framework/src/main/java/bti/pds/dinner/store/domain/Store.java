package bti.pds.dinner.store.domain;

import bti.pds.dinner.store.domain.exception.InvalidStoreConfigurationException;
import lombok.Getter;

import java.time.LocalTime;
import java.util.Objects;

@Getter
public class Store {
    private Long id;
    private String name;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private int maxOrdersInProgress;
    private boolean automaticPause;
    private StoreStatus status;
    private StoreStatus manualStatus;
    private String addressStreet;
    private String addressNumber;
    private String addressNeighborhood;
    private String addressCity;
    private String addressZipCode;

    public Store(
            Long id,
            String name,
            LocalTime openingTime,
            LocalTime closingTime,
            int maxOrdersInProgress,
            boolean automaticPause,
            StoreStatus status,
            StoreStatus manualStatus,
            String addressStreet,
            String addressNumber,
            String addressNeighborhood,
            String addressCity,
            String addressZipCode
    ) {
        validateName(name);
        validateSettings(openingTime, closingTime, maxOrdersInProgress);
        this.id = id;
        this.name = name;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.maxOrdersInProgress = maxOrdersInProgress;
        this.automaticPause = automaticPause;
        this.status = Objects.requireNonNull(status, "Status is required");
        this.manualStatus = manualStatus;
        this.addressStreet = addressStreet;
        this.addressNumber = addressNumber;
        this.addressNeighborhood = addressNeighborhood;
        this.addressCity = addressCity;
        this.addressZipCode = addressZipCode;
    }

    public Store(
            Long id,
            String name,
            LocalTime openingTime,
            LocalTime closingTime,
            int maxOrdersInProgress,
            boolean automaticPause,
            StoreStatus status,
            String addressStreet,
            String addressNumber,
            String addressNeighborhood,
            String addressCity,
            String addressZipCode
    ) {
        this(id, name, openingTime, closingTime, maxOrdersInProgress, automaticPause, status, null, addressStreet, addressNumber, addressNeighborhood, addressCity, addressZipCode);
    }

    public void updateSettings(
            LocalTime openingTime,
            LocalTime closingTime,
            int maxOrdersInProgress,
            boolean automaticPause
    ) {
        validateSettings(openingTime, closingTime, maxOrdersInProgress);
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.maxOrdersInProgress = maxOrdersInProgress;
        this.automaticPause = automaticPause;
    }

    public void setManualStatus(StoreStatus status) {
        this.manualStatus = Objects.requireNonNull(status, "Manual status is required");
        this.status = status;
    }

    public void clearManualStatus() {
        this.manualStatus = null;
    }

    public boolean refreshAutomaticStatus(LocalTime currentTime, int ordersInProgress) {
        Objects.requireNonNull(currentTime, "Current time is required");
        if (ordersInProgress < 0) {
            throw new IllegalArgumentException("Orders in progress cannot be negative");
        }
        if (manualStatus != null) {
            return false;
        }

        StoreStatus calculatedStatus = calculateAutomaticStatus(currentTime, ordersInProgress);

        if (status == calculatedStatus) {
            return false;
        }
        status = calculatedStatus;
        return true;
    }

    public boolean refreshAutomaticStatus(LocalTime currentTime) {
        return refreshAutomaticStatus(currentTime, 0);
    }

    private StoreStatus calculateAutomaticStatus(LocalTime currentTime, int ordersInProgress) {
        if (!isOpenAt(currentTime)) {
            return StoreStatus.FECHADA;
        }
        if (automaticPause && ordersInProgress >= maxOrdersInProgress) {
            return StoreStatus.PAUSADA;
        }
        return StoreStatus.ABERTA;
    }

    private boolean isOpenAt(LocalTime currentTime) {
        if (openingTime.equals(closingTime)) {
            return true;
        }
        if (openingTime.isBefore(closingTime)) {
            return !currentTime.isBefore(openingTime) && currentTime.isBefore(closingTime);
        }
        return !currentTime.isBefore(openingTime) || currentTime.isBefore(closingTime);
    }

    private void validateSettings(
            LocalTime openingTime,
            LocalTime closingTime,
            int maxOrdersInProgress
    ) {
        if (openingTime == null || closingTime == null) {
            throw new InvalidStoreConfigurationException("Opening and closing times are required");
        }
        if (maxOrdersInProgress <= 0) {
            throw new InvalidStoreConfigurationException("Maximum orders in progress must be greater than zero");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidStoreConfigurationException("Store name is required");
        }
    }
}
