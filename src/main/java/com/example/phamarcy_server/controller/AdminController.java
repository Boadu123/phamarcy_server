package com.example.phamarcy_server.controller;

import com.example.phamarcy_server.dto.AdminDashboardResponse;
import com.example.phamarcy_server.dto.InventoryResponse;
import com.example.phamarcy_server.dto.PharmacyDashboardResponse;
import com.example.phamarcy_server.dto.SaleResponse;
import com.example.phamarcy_server.service.AdminReportingService;
import com.example.phamarcy_server.util.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.ADMIN)
@Tag(name = "Administration", description = "Read-only reporting endpoints for administrators")
public class AdminController {

    private final AdminReportingService adminReportingService;

    public AdminController(AdminReportingService adminReportingService) {
        this.adminReportingService = adminReportingService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get global dashboard totals", responses = {
            @ApiResponse(responseCode = "200", description = "Dashboard summary returned")
    })
    public AdminDashboardResponse dashboard() {
        return adminReportingService.getDashboard();
    }

    @GetMapping("/pharmacies/{pharmacyId}/inventory")
    @Operation(summary = "Get inventory records for a pharmacy", responses = {
            @ApiResponse(responseCode = "200", description = "Inventory records returned"),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found")
    })
    public List<InventoryResponse> pharmacyInventory(@PathVariable UUID pharmacyId) {
        return adminReportingService.getInventory(pharmacyId);
    }

    @GetMapping("/pharmacies/{pharmacyId}/sales")
    @Operation(summary = "Get sales records for a pharmacy", responses = {
            @ApiResponse(responseCode = "200", description = "Sales records returned"),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found")
    })
    public List<SaleResponse> pharmacySales(@PathVariable UUID pharmacyId) {
        return adminReportingService.getSales(pharmacyId);
    }

    @GetMapping("/pharmacies/{pharmacyId}/dashboard")
    @Operation(summary = "Get dashboard totals for a pharmacy", responses = {
            @ApiResponse(responseCode = "200", description = "Pharmacy dashboard returned"),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found")
    })
    public PharmacyDashboardResponse pharmacyDashboard(@PathVariable UUID pharmacyId) {
        return adminReportingService.getPharmacyDashboard(pharmacyId);
    }
}