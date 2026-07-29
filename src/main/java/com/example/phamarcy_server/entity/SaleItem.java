package com.example.phamarcy_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sale_items")
public class SaleItem {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "batch_number", nullable = false, length = 120)
    private String batchNumber;

    @Column(name = "quantity_sold", nullable = false)
    private Integer quantitySold;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    private boolean deleted;

    protected SaleItem() {
    }

    public SaleItem(UUID id, Pharmacy pharmacy, Sale sale, Batch batch, String productName, String batchNumber, Integer quantitySold, BigDecimal unitPrice, Instant createdAt, Instant lastUpdatedAt) {
        this.id = id;
        this.pharmacy = pharmacy;
        this.createdAt = createdAt;
        update(sale, batch, productName, batchNumber, quantitySold, unitPrice, lastUpdatedAt);
    }

    public void update(Sale sale, Batch batch, String productName, String batchNumber, Integer quantitySold, BigDecimal unitPrice, Instant lastUpdatedAt) {
        this.sale = sale;
        this.batch = batch;
        this.productName = productName;
        this.batchNumber = batchNumber;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
        this.lastUpdatedAt = lastUpdatedAt;
        this.deleted = false;
    }

    public UUID getId() {
        return id;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public Sale getSale() {
        return sale;
    }

    public Batch getBatch() {
        return batch;
    }

    public String getProductName() {
        return productName;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public Integer getQuantitySold() {
        return quantitySold;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
