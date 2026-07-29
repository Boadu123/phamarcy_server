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
@Table(name = "users")
public class UserAccount {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    private boolean deleted;

    protected UserAccount() {
    }

    public UserAccount(UUID id, Pharmacy pharmacy, String username, String passwordHash, String role, Instant createdAt, Instant lastUpdatedAt) {
        this.id = id;
        this.pharmacy = pharmacy;
        update(username, passwordHash, role, createdAt, lastUpdatedAt);
    }

    public void update(String username, String passwordHash, String role, Instant createdAt, Instant lastUpdatedAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
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

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
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
