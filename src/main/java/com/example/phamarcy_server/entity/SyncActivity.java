package com.example.phamarcy_server.entity;

import com.example.phamarcy_server.dto.SyncActivityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_activities")
public class SyncActivity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "pharmacy_id", nullable = false, updatable = false)
    private UUID pharmacyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncActivityStatus status;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "records_received", nullable = false)
    private Integer recordsReceived;

    @Column(name = "records_inserted", nullable = false)
    private Integer recordsInserted;

    @Column(name = "records_updated", nullable = false)
    private Integer recordsUpdated;

    @Column(name = "records_ignored", nullable = false)
    private Integer recordsIgnored;

    @Column(name = "inventory_records_received", nullable = false)
    private Integer inventoryRecordsReceived;

    @Column(name = "inventory_records_applied", nullable = false)
    private Integer inventoryRecordsApplied;

    @Column(name = "sales_records_received", nullable = false)
    private Integer salesRecordsReceived;

    @Column(name = "sales_records_applied", nullable = false)
    private Integer salesRecordsApplied;

    @Column(nullable = false, length = 1000)
    private String message;

    protected SyncActivity() {
    }

    public SyncActivity(UUID id, UUID pharmacyId, Instant startedAt, int recordsReceived,
            int inventoryRecordsReceived, int salesRecordsReceived) {
        this.id = id;
        this.pharmacyId = pharmacyId;
        this.status = SyncActivityStatus.IN_PROGRESS;
        this.startedAt = startedAt;
        this.recordsReceived = recordsReceived;
        this.recordsInserted = 0;
        this.recordsUpdated = 0;
        this.recordsIgnored = 0;
        this.inventoryRecordsReceived = inventoryRecordsReceived;
        this.inventoryRecordsApplied = 0;
        this.salesRecordsReceived = salesRecordsReceived;
        this.salesRecordsApplied = 0;
        this.message = "Synchronization is in progress";
    }

    public void markSuccessful(Instant completedAt, int recordsInserted, int recordsUpdated,
            int recordsIgnored, int inventoryRecordsApplied, int salesRecordsApplied) {
        this.status = SyncActivityStatus.SUCCESSFUL;
        this.completedAt = completedAt;
        this.recordsInserted = recordsInserted;
        this.recordsUpdated = recordsUpdated;
        this.recordsIgnored = recordsIgnored;
        this.inventoryRecordsApplied = inventoryRecordsApplied;
        this.salesRecordsApplied = salesRecordsApplied;
        this.message = "Synchronization completed successfully";
    }

    public void markFailed(Instant completedAt, String message) {
        this.status = SyncActivityStatus.FAILED;
        this.completedAt = completedAt;
        this.message = message;
    }

    public UUID getId() { return id; }
    public UUID getPharmacyId() { return pharmacyId; }
    public SyncActivityStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Integer getRecordsReceived() { return recordsReceived; }
    public Integer getRecordsInserted() { return recordsInserted; }
    public Integer getRecordsUpdated() { return recordsUpdated; }
    public Integer getRecordsIgnored() { return recordsIgnored; }
    public Integer getInventoryRecordsReceived() { return inventoryRecordsReceived; }
    public Integer getInventoryRecordsApplied() { return inventoryRecordsApplied; }
    public Integer getSalesRecordsReceived() { return salesRecordsReceived; }
    public Integer getSalesRecordsApplied() { return salesRecordsApplied; }
    public String getMessage() { return message; }

    public Long getDurationMs() {
        return completedAt == null ? null : Duration.between(startedAt, completedAt).toMillis();
    }
}
