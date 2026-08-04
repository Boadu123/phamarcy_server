package com.example.phamarcy_server.service.impl;

import com.example.phamarcy_server.dto.AdminDashboardResponse;
import com.example.phamarcy_server.dto.InventoryResponse;
import com.example.phamarcy_server.dto.InventoryStatisticsResponse;
import com.example.phamarcy_server.dto.PharmacyDashboardResponse;
import com.example.phamarcy_server.dto.PharmacyDetailsResponse;
import com.example.phamarcy_server.dto.PharmacySummaryResponse;
import com.example.phamarcy_server.dto.SaleDetailsResponse;
import com.example.phamarcy_server.dto.SaleItemResponse;
import com.example.phamarcy_server.dto.SaleResponse;
import com.example.phamarcy_server.dto.SalesStatisticsResponse;
import com.example.phamarcy_server.dto.SyncActivityResponse;
import com.example.phamarcy_server.dto.SyncActivityStatus;
import com.example.phamarcy_server.entity.Batch;
import com.example.phamarcy_server.entity.Pharmacy;
import com.example.phamarcy_server.entity.Sale;
import com.example.phamarcy_server.entity.SaleItem;
import com.example.phamarcy_server.entity.SyncActivity;
import com.example.phamarcy_server.exception.PharmacyNotFoundException;
import com.example.phamarcy_server.exception.SaleNotFoundException;
import com.example.phamarcy_server.repository.BatchRepository;
import com.example.phamarcy_server.repository.PharmacyRepository;
import com.example.phamarcy_server.repository.SaleItemRepository;
import com.example.phamarcy_server.repository.SaleRepository;
import com.example.phamarcy_server.repository.SyncActivityRepository;
import com.example.phamarcy_server.service.AdminReportingService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminReportingServiceImpl implements AdminReportingService {

    private static final int DASHBOARD_ACTIVITY_LIMIT = 10;

    private final PharmacyRepository pharmacyRepository;
    private final BatchRepository batchRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final SyncActivityRepository syncActivityRepository;

    public AdminReportingServiceImpl(
            PharmacyRepository pharmacyRepository,
            BatchRepository batchRepository,
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository,
            SyncActivityRepository syncActivityRepository
    ) {
        this.pharmacyRepository = pharmacyRepository;
        this.batchRepository = batchRepository;
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.syncActivityRepository = syncActivityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        List<SyncActivityResponse> recentActivity = getSyncActivity(DASHBOARD_ACTIVITY_LIMIT);
        return new AdminDashboardResponse(
                Instant.now(),
                pharmacyRepository.count(),
                syncActivityRepository.countDistinctPharmacyIdByStatus(SyncActivityStatus.SUCCESSFUL),
                batchRepository.countByDeletedFalse(),
                zeroIfNull(batchRepository.calculateTotalUnitsInStock()),
                zeroIfNull(batchRepository.calculateTotalInventoryValue()),
                saleRepository.countByDeletedFalse(),
                zeroIfNull(saleRepository.calculateTotalSalesAmount()),
                syncActivityRepository.countByStatus(SyncActivityStatus.SUCCESSFUL),
                syncActivityRepository.countByStatus(SyncActivityStatus.FAILED),
                syncActivityRepository.countByStatus(SyncActivityStatus.IN_PROGRESS),
                recentActivity.isEmpty() ? null : recentActivity.getFirst(),
                recentActivity
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacySummaryResponse> getPharmacies() {
        return pharmacyRepository.findAllByOrderByNameAsc().stream()
                .map(this::toPharmacySummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyDetailsResponse getPharmacyDetails(UUID pharmacyId) {
        Pharmacy pharmacy = getPharmacy(pharmacyId);
        List<SyncActivityResponse> recentActivity = getSyncActivity(pharmacyId, DASHBOARD_ACTIVITY_LIMIT);
        SyncActivityResponse latestSync = recentActivity.isEmpty() ? null : recentActivity.getFirst();

        return new PharmacyDetailsResponse(
                pharmacy.getId(),
                pharmacy.getName(),
                pharmacy.getLocation(),
                statusOf(latestSync),
                lastSyncAt(latestSync),
                inventoryStatistics(pharmacyId),
                salesStatistics(pharmacyId),
                syncActivityRepository.countByPharmacyIdAndStatus(pharmacyId, SyncActivityStatus.SUCCESSFUL),
                syncActivityRepository.countByPharmacyIdAndStatus(pharmacyId, SyncActivityStatus.FAILED),
                latestSync,
                recentActivity
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncActivityResponse> getSyncActivity(int limit) {
        return syncActivityRepository.findAllByOrderByStartedAtDesc(PageRequest.of(0, limit)).stream()
                .map(this::toSyncActivityResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncActivityResponse> getSyncActivity(UUID pharmacyId, int limit) {
        ensurePharmacyExists(pharmacyId);
        return syncActivityRepository.findByPharmacyIdOrderByStartedAtDesc(pharmacyId, PageRequest.of(0, limit)).stream()
                .map(this::toSyncActivityResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventory(UUID pharmacyId) {
        ensurePharmacyExists(pharmacyId);
        return batchRepository.findActiveInventory(pharmacyId).stream()
                .map(this::toInventoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getSales(UUID pharmacyId) {
        ensurePharmacyExists(pharmacyId);
        return saleRepository.findActiveSales(pharmacyId).stream()
                .map(this::toSaleResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaleDetailsResponse getSaleDetails(UUID pharmacyId, UUID saleId) {
        ensurePharmacyExists(pharmacyId);
        Sale sale = saleRepository.findActiveByIdAndPharmacyId(saleId, pharmacyId)
                .orElseThrow(() -> new SaleNotFoundException(pharmacyId, saleId));
        List<SaleItemResponse> items = saleItemRepository.findActiveBySaleIdAndPharmacyId(saleId, pharmacyId).stream()
                .map(this::toSaleItemResponse)
                .toList();

        return new SaleDetailsResponse(
                sale.getId(),
                sale.getPharmacy().getId(),
                sale.getPharmacy().getName(),
                sale.getPharmacy().getLocation(),
                sale.getUser().getId(),
                sale.getUser().getUsername(),
                sale.getTotalAmount(),
                sale.getSaleDate(),
                items.size(),
                sale.getCreatedAt(),
                sale.getLastUpdatedAt(),
                items
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyDashboardResponse getPharmacyDashboard(UUID pharmacyId) {
        Pharmacy pharmacy = getPharmacy(pharmacyId);
        SyncActivityResponse latestSync = syncActivityRepository.findTopByPharmacyIdOrderByStartedAtDesc(pharmacyId)
                .map(this::toSyncActivityResponse)
                .orElse(null);
        InventoryStatisticsResponse inventory = inventoryStatistics(pharmacyId);
        SalesStatisticsResponse sales = salesStatistics(pharmacyId);

        return new PharmacyDashboardResponse(
                pharmacy.getId(),
                pharmacy.getName(),
                pharmacy.getLocation(),
                statusOf(latestSync),
                lastSyncAt(latestSync),
                inventory.totalRecords(),
                inventory.totalUnitsInStock(),
                inventory.totalValue(),
                sales.totalTransactions(),
                sales.totalAmount(),
                syncActivityRepository.countByPharmacyIdAndStatus(pharmacyId, SyncActivityStatus.SUCCESSFUL),
                syncActivityRepository.countByPharmacyIdAndStatus(pharmacyId, SyncActivityStatus.FAILED)
        );
    }

    private PharmacySummaryResponse toPharmacySummary(Pharmacy pharmacy) {
        UUID pharmacyId = pharmacy.getId();
        SyncActivityResponse latestSync = syncActivityRepository.findTopByPharmacyIdOrderByStartedAtDesc(pharmacyId)
                .map(this::toSyncActivityResponse)
                .orElse(null);
        InventoryStatisticsResponse inventory = inventoryStatistics(pharmacyId);
        SalesStatisticsResponse sales = salesStatistics(pharmacyId);
        return new PharmacySummaryResponse(
                pharmacyId,
                pharmacy.getName(),
                pharmacy.getLocation(),
                statusOf(latestSync),
                lastSyncAt(latestSync),
                inventory.totalRecords(),
                inventory.totalUnitsInStock(),
                inventory.totalValue(),
                sales.totalTransactions(),
                sales.totalAmount()
        );
    }

    private InventoryStatisticsResponse inventoryStatistics(UUID pharmacyId) {
        return new InventoryStatisticsResponse(
                batchRepository.countByPharmacy_IdAndDeletedFalse(pharmacyId),
                zeroIfNull(batchRepository.calculateTotalUnitsInStockByPharmacyId(pharmacyId)),
                zeroIfNull(batchRepository.calculateTotalInventoryValueByPharmacyId(pharmacyId))
        );
    }

    private SalesStatisticsResponse salesStatistics(UUID pharmacyId) {
        return new SalesStatisticsResponse(
                saleRepository.countByPharmacy_IdAndDeletedFalse(pharmacyId),
                zeroIfNull(saleRepository.calculateTotalSalesAmountByPharmacyId(pharmacyId))
        );
    }

    private InventoryResponse toInventoryResponse(Batch batch) {
        return new InventoryResponse(
                batch.getId(),
                batch.getPharmacy().getId(),
                batch.getProduct().getId(),
                batch.getProduct().getName(),
                batch.getProduct().getCategory(),
                batch.getStockReference(),
                batch.getBatchNumber(),
                batch.getQuantity(),
                batch.getCostPrice(),
                batch.getSellingPrice(),
                batch.getSellingPrice().multiply(BigDecimal.valueOf(batch.getQuantity())),
                batch.getExpiryDate(),
                batch.getLastUpdatedAt()
        );
    }

    private SaleResponse toSaleResponse(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getPharmacy().getId(),
                sale.getUser().getId(),
                sale.getUser().getUsername(),
                sale.getTotalAmount(),
                sale.getSaleDate(),
                sale.getItems().size(),
                sale.getLastUpdatedAt()
        );
    }

    private SaleItemResponse toSaleItemResponse(SaleItem item) {
        return new SaleItemResponse(
                item.getId(),
                item.getBatch().getProduct().getId(),
                item.getProductName(),
                item.getBatch().getId(),
                item.getBatch().getStockReference(),
                item.getBatchNumber(),
                item.getQuantitySold(),
                item.getUnitPrice(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantitySold().longValue())),
                item.getCreatedAt(),
                item.getLastUpdatedAt()
        );
    }

    private SyncActivityResponse toSyncActivityResponse(SyncActivity activity) {
        String pharmacyName = pharmacyRepository.findById(activity.getPharmacyId())
                .map(Pharmacy::getName)
                .orElse("Unregistered pharmacy");
        return new SyncActivityResponse(
                activity.getId(),
                activity.getPharmacyId(),
                pharmacyName,
                activity.getStatus(),
                activity.getStartedAt(),
                activity.getCompletedAt(),
                activity.getDurationMs(),
                activity.getRecordsReceived(),
                activity.getRecordsInserted(),
                activity.getRecordsUpdated(),
                activity.getRecordsIgnored(),
                activity.getInventoryRecordsReceived(),
                activity.getInventoryRecordsApplied(),
                activity.getSalesRecordsReceived(),
                activity.getSalesRecordsApplied(),
                activity.getMessage()
        );
    }

    private SyncActivityStatus statusOf(SyncActivityResponse latestSync) {
        return latestSync == null ? SyncActivityStatus.NEVER_SYNCED : latestSync.status();
    }

    private Instant lastSyncAt(SyncActivityResponse latestSync) {
        if (latestSync == null) {
            return null;
        }
        return latestSync.completedAt() == null ? latestSync.startedAt() : latestSync.completedAt();
    }

    private Pharmacy getPharmacy(UUID pharmacyId) {
        return pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyNotFoundException(pharmacyId));
    }

    private void ensurePharmacyExists(UUID pharmacyId) {
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new PharmacyNotFoundException(pharmacyId);
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }
}
