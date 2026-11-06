package bti.pds.dinner.stock.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Stock {
    private StockId id;
    private String name;

    public Stock(String name) {
        this.id = null;
        this.name = name;
    }
}
