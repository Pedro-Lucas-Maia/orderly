package bti.pds.dinner.store.domain;

import java.time.LocalTime;
import java.util.Objects;

import bti.pds.dinner.store.domain.exception.InvalidStoreConfigurationException;
import lombok.Getter;

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

    public Store(
            Long id,
            String name,
            LocalTime openingTime,
            LocalTime closingTime,
            int maxOrdersInProgress,
            boolean automaticPause,
            StoreStatus status,
            StoreStatus manualStatus
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
    }

    public Store(
            Long id,
            String name,
            LocalTime openingTime,
            LocalTime closingTime,
            int maxOrdersInProgress,
            boolean automaticPause,
            StoreStatus status
    ) {
        this(id, name, openingTime, closingTime, maxOrdersInProgress, automaticPause, status, null);
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

    public boolean refreshAutomaticStatus(LocalTime currentTime) {
        Objects.requireNonNull(currentTime, "Current time is required");
        if (manualStatus != null) {
            return false;
        }

        StoreStatus calculatedStatus = isOpenAt(currentTime)
                ? StoreStatus.ABERTA
                : StoreStatus.FECHADA;

        if (status == calculatedStatus) {
            return false;
        }
        status = calculatedStatus;
        return true;
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
