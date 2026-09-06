package bti.pds.dinner.sales.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import bti.pds.dinner.sales.domain.SaleStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor 
@Getter
@Setter
@Entity
@Table(name = "sales")
public class SaleEntity {
    @Id 
    private String id;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SaleStatus status;

    @Column(name = "total_value", nullable = false)
    private BigDecimal valorTotal;

    @Column(name = "observation")
    private String observation;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItemEntity> itens = new ArrayList<>();

    public void addItem(SaleItemEntity item){
        item.setSale(this);
        this.itens.add(item);
    }
}
