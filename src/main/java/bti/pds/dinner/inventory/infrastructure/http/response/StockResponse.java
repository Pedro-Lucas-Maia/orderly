package bti.pds.dinner.inventory.infrastructure.http.response;

import bti.pds.dinner.inventory.application.output.StockOutput;
import org.jspecify.annotations.NonNull;

public record StockResponse(Long id, String name) {
    public static StockResponse from(@NonNull StockOutput output) {
        return new StockResponse(output.id(), output.name());
    }
}
