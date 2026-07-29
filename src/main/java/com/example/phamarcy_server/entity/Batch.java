package com.example.phamarcy_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "batches",
        uniqueConstraints = @UniqueConstraint(name = "ux_batches_pharmacy_stock_reference", columnNames = {"pharmacy_id", "stock_reference"})
)
public class Batch {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "stock_reference", nullable = false, length = 64)
    private String stockReference;

    @Column(name = "batch_number", nullable = false, length = 120)
    private String batchNumber;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "cost_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    private boolean deleted;

    protected Batch() {
    }

    public Batch(UUID id, Pharmacy pharmacy, Product product, String stockReference, String batchNumber, Integer quantity, BigDecimal costPrice, BigDecimal sellingPrice, LocalDate expiryDate, Instant createdAt, Instant lastUpdatedAt) {
        this.id = id;
        this.pharmacy = pharmacy;
        this.createdAt = createdAt;
        update(product, stockReference, batchNumber, quantity, costPrice, sellingPrice, expiryDate, lastUpdatedAt);
    }

    public void update(Product product, String stockReference, String batchNumber, Integer quantity, BigDecimal costPrice, BigDecimal sellingPrice, LocalDate expiryDate, Instant lastUpdatedAt) {
        this.product = product;
        this.stockReference = stockReference;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.expiryDate = expiryDate;
        this.lastUpdatedAt = lastUpdatedAt;
        this.deleted = false;
    }

    public UUID getId() {
        return id;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public Product getProduct() {
        return product;
    }

    public String getStockReference() {
        return stockReference;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
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
