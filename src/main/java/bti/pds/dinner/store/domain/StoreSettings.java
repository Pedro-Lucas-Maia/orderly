package bti.pds.dinner.store.domain;

import java.time.LocalTime;

public class StoreSettings {
    private LocalTime openingTime;
    private LocalTime closingTime;
    private int maxOrdersInProgress;
    private boolean automaticPause;
}
