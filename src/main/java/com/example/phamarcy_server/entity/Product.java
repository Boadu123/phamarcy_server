package com.example.phamarcy_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 120)
    private String category;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    private boolean deleted;

    protected Product() {
    }

    public Product(UUID id, Pharmacy pharmacy, String name, String description, String category, Integer reorderLevel, Instant createdAt, Instant lastUpdatedAt) {
        this.id = id;
        this.pharmacy = pharmacy;
        update(name, description, category, reorderLevel, createdAt, lastUpdatedAt);
    }

    public void update(String name, String description, String category, Integer reorderLevel, Instant createdAt, Instant lastUpdatedAt) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.reorderLevel = reorderLevel;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
        this.deleted = false;
    }

    public UUID getId() {
        return id;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
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
