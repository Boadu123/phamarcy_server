package com.example.phamarcy_server.service;

import com.example.phamarcy_server.dto.AdminDashboardResponse;
import com.example.phamarcy_server.dto.InventoryResponse;
import com.example.phamarcy_server.dto.PharmacyDashboardResponse;
import com.example.phamarcy_server.dto.SaleResponse;
import java.util.List;
import java.util.UUID;

public interface AdminReportingService {

    AdminDashboardResponse getDashboard();

    List<InventoryResponse> getInventory(UUID pharmacyId);

    List<SaleResponse> getSales(UUID pharmacyId);

    PharmacyDashboardResponse getPharmacyDashboard(UUID pharmacyId);
}