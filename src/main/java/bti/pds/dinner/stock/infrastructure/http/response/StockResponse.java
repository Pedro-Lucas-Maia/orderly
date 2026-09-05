package bti.pds.dinner.stock.infrastructure.http.response;

import org.jspecify.annotations.NonNull;

import bti.pds.dinner.stock.application.output.StockOutput;

public record StockResponse(Long id, String name) {
    public static StockResponse from(@NonNull StockOutput output) {
        return new StockResponse(output.id(), output.name());
    }
}
