package com.example.phamarcy_server.service.impl;

import com.example.phamarcy_server.dto.AdminDashboardResponse;
import com.example.phamarcy_server.dto.InventoryResponse;
import com.example.phamarcy_server.dto.PharmacyDashboardResponse;
import com.example.phamarcy_server.dto.SaleResponse;
import com.example.phamarcy_server.entity.Pharmacy;
import com.example.phamarcy_server.exception.PharmacyNotFoundException;
import com.example.phamarcy_server.mapper.CentralInventoryMapper;
import com.example.phamarcy_server.mapper.CentralSaleMapper;
import com.example.phamarcy_server.repository.CentralInventoryRepository;
import com.example.phamarcy_server.repository.CentralSaleRepository;
import com.example.phamarcy_server.repository.PharmacyRepository;
import com.example.phamarcy_server.service.AdminReportingService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminReportingServiceImpl implements AdminReportingService {

    private final PharmacyRepository pharmacyRepository;
    private final CentralInventoryRepository inventoryRepository;
    private final CentralSaleRepository saleRepository;
    private final CentralInventoryMapper inventoryMapper;
    private final CentralSaleMapper saleMapper;

    public AdminReportingServiceImpl(
            PharmacyRepository pharmacyRepository,
            CentralInventoryRepository inventoryRepository,
            CentralSaleRepository saleRepository,
            CentralInventoryMapper inventoryMapper,
            CentralSaleMapper saleMapper
    ) {
        this.pharmacyRepository = pharmacyRepository;
        this.inventoryRepository = inventoryRepository;
        this.saleRepository = saleRepository;
        this.inventoryMapper = inventoryMapper;
        this.saleMapper = saleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        return new AdminDashboardResponse(
                pharmacyRepository.count(),
                inventoryRepository.count(),
                zeroIfNull(inventoryRepository.calculateTotalInventoryValue()),
                saleRepository.count(),
                zeroIfNull(saleRepository.calculateTotalSalesAmount())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventory(UUID pharmacyId) {
        ensurePharmacyExists(pharmacyId);
        return inventoryRepository.findByPharmacyIdOrderByProductNameAsc(pharmacyId)
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getSales(UUID pharmacyId) {
        ensurePharmacyExists(pharmacyId);
        return saleRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId)
                .stream()
                .map(saleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyDashboardResponse getPharmacyDashboard(UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyNotFoundException(pharmacyId));

        return new PharmacyDashboardResponse(
                pharmacy.getId(),
                pharmacy.getName(),
                pharmacy.getLocation(),
                inventoryRepository.countByPharmacyId(pharmacyId),
                zeroIfNull(inventoryRepository.calculateTotalInventoryValueByPharmacyId(pharmacyId)),
                saleRepository.countByPharmacyId(pharmacyId),
                zeroIfNull(saleRepository.calculateTotalSalesAmountByPharmacyId(pharmacyId))
        );
    }

    private void ensurePharmacyExists(UUID pharmacyId) {
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new PharmacyNotFoundException(pharmacyId);
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}