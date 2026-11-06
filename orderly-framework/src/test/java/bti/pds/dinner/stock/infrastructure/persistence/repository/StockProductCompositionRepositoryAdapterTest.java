package bti.pds.dinner.stock.infrastructure.persistence.repository;

import bti.pds.dinner.stock.domain.StockItemId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class StockProductCompositionRepositoryAdapterTest {

    private bti.pds.dinner.product.application.service.ProductCompositionService productCompositionRepository;
    private StockProductCompositionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        productCompositionRepository = mock(bti.pds.dinner.product.application.service.ProductCompositionService.class);
        adapter = new StockProductCompositionRepositoryAdapter(productCompositionRepository);
    }

    @Test
    void deleteByStockItemId_callsProductCompositionRepository_whenIdIsValid() {
        adapter.deleteByStockItemId(new StockItemId(10L));

        verify(productCompositionRepository, times(1))
                .deleteByStockItemId(10L);
    }

    @Test
    void deleteByStockItemId_doesNotCallRepository_whenIdIsNull() {
        adapter.deleteByStockItemId(null);
        adapter.deleteByStockItemId(new StockItemId(null));

        verifyNoInteractions(productCompositionRepository);
    }
}
