package com.example.phamarcy_server.controller;

import com.example.phamarcy_server.dto.AdminDashboardResponse;
import com.example.phamarcy_server.dto.InventoryResponse;
import com.example.phamarcy_server.dto.PharmacyDashboardResponse;
import com.example.phamarcy_server.dto.SaleResponse;
import com.example.phamarcy_server.service.AdminReportingService;
import com.example.phamarcy_server.util.ApiPaths;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.ADMIN)
public class AdminController {

    private final AdminReportingService adminReportingService;

    public AdminController(AdminReportingService adminReportingService) {
        this.adminReportingService = adminReportingService;
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return adminReportingService.getDashboard();
    }

    @GetMapping("/pharmacies/{pharmacyId}/inventory")
    public List<InventoryResponse> pharmacyInventory(@PathVariable UUID pharmacyId) {
        return adminReportingService.getInventory(pharmacyId);
    }

    @GetMapping("/pharmacies/{pharmacyId}/sales")
    public List<SaleResponse> pharmacySales(@PathVariable UUID pharmacyId) {
        return adminReportingService.getSales(pharmacyId);
    }

    @GetMapping("/pharmacies/{pharmacyId}/dashboard")
    public PharmacyDashboardResponse pharmacyDashboard(@PathVariable UUID pharmacyId) {
        return adminReportingService.getPharmacyDashboard(pharmacyId);
    }
}