package bti.pds.dinner.product.infrastructure.persistence.repository;

import bti.pds.dinner.product.domain.StockItemId;
import bti.pds.dinner.stock.application.output.StockItemOutput;
import bti.pds.dinner.stock.application.service.StockItemService;
import bti.pds.dinner.stock.domain.exception.StockItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductStockItemRepositoryAdapterTest {

    private StockItemService stockItemService;
    private ProductStockItemRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        stockItemService = mock(StockItemService.class);
        adapter = new ProductStockItemRepositoryAdapter(stockItemService);
    }

    @Test
    void existsById_returnsTrue_whenItemExists() {
        StockItemOutput output = new StockItemOutput(
                10L, 1L, "Tomato", "Veg", "KG", 10, 2, new BigDecimal("3.00"), true
        );
        when(stockItemService.getById(10L)).thenReturn(output);

        assertTrue(adapter.existsById(new StockItemId(10L)));
        assertTrue(adapter.existsById(10L));
    }

    @Test
    void existsById_propagatesException_whenItemNotFound() {
        when(stockItemService.getById(99L)).thenThrow(new StockItemNotFoundException("Stock item not found"));

        assertThrows(StockItemNotFoundException.class, () -> adapter.existsById(new StockItemId(99L)));
        assertThrows(StockItemNotFoundException.class, () -> adapter.existsById(99L));
    }
}
