package bti.pds.dinner.inventory.application.service;

import bti.pds.dinner.inventory.application.input.CreateStockInput;
import bti.pds.dinner.inventory.application.output.StockOutput;
import bti.pds.dinner.inventory.domain.Stock;
import bti.pds.dinner.inventory.domain.StockId;
import bti.pds.dinner.inventory.domain.StockRepository;
import bti.pds.dinner.inventory.domain.exception.StockNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {
    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public StockOutput create(CreateStockInput input) {
        Stock saved = stockRepository.save(new Stock(input.name()));
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

    private StockOutput toOutput(Stock stock) {
        return new StockOutput(stock.getId().value(), stock.getName());
    }
}
