package bti.pds.dinner.store.domain;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
@AllArgsConstructor
public class Store {
    private Long id;
    private String name;
    private LocalTime openinTime;
    private LocalTime closingTIme;
    private int maxOrdersInProgress;
    private boolean automaticPause;
    private StoreStatus status;
}
