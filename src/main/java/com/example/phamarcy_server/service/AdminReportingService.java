package com.example.phamarcy_server.service;

import com.example.phamarcy_server.dto.AdminDashboardResponse;
import com.example.phamarcy_server.dto.InventoryResponse;
import com.example.phamarcy_server.dto.PharmacyDetailsResponse;
import com.example.phamarcy_server.dto.PharmacyDashboardResponse;
import com.example.phamarcy_server.dto.PharmacySummaryResponse;
import com.example.phamarcy_server.dto.SaleDetailsResponse;
import com.example.phamarcy_server.dto.SaleResponse;
import com.example.phamarcy_server.dto.SyncActivityResponse;
import java.util.List;
import java.util.UUID;

public interface AdminReportingService {

    AdminDashboardResponse getDashboard();

    List<PharmacySummaryResponse> getPharmacies();

    PharmacyDetailsResponse getPharmacyDetails(UUID pharmacyId);

    List<SyncActivityResponse> getSyncActivity(int limit);

    List<SyncActivityResponse> getSyncActivity(UUID pharmacyId, int limit);

    List<InventoryResponse> getInventory(UUID pharmacyId);

    List<SaleResponse> getSales(UUID pharmacyId);

    SaleDetailsResponse getSaleDetails(UUID pharmacyId, UUID saleId);

    PharmacyDashboardResponse getPharmacyDashboard(UUID pharmacyId);
}
