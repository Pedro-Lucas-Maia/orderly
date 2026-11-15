package bti.pds.dinner.stock.application.service;

import bti.pds.dinner.stock.application.input.CreateStockInput;
import bti.pds.dinner.stock.application.output.StockOutput;
import bti.pds.dinner.stock.domain.Stock;
import bti.pds.dinner.stock.domain.StockId;
import bti.pds.dinner.stock.domain.StockRepository;
import bti.pds.dinner.stock.domain.exception.StockNotFoundException;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class StockService {
    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    @Observed(name = "stock.admin.create")
    public StockOutput create(@NonNull CreateStockInput input) {
        Stock saved = stockRepository.save(new Stock(input.name()));
        log.info("stock created. stockId={}", saved.getId());
        return toOutput(saved);
    }

    public List<StockOutput> list() {
        return stockRepository.findAll().stream()
                .map(this::toOutput)
                .toList();
    }

    public StockOutput getById(Long id) {
        Stock stock = stockRepository.findById(new StockId(id))
                .orElseThrow(() -> new StockNotFoundException("Stock not found"));
        return toOutput(stock);
    }

    private @NonNull StockOutput toOutput(@NonNull Stock stock) {
        return new StockOutput(stock.getId().value(), stock.getName());
    }
}
