package com.example.phamarcy_server.controller;

import com.example.phamarcy_server.dto.AdminDashboardResponse;
import com.example.phamarcy_server.dto.ApiError;
import com.example.phamarcy_server.dto.InventoryResponse;
import com.example.phamarcy_server.dto.PharmacyDetailsResponse;
import com.example.phamarcy_server.dto.PharmacyDashboardResponse;
import com.example.phamarcy_server.dto.PharmacySummaryResponse;
import com.example.phamarcy_server.dto.SaleDetailsResponse;
import com.example.phamarcy_server.dto.SaleResponse;
import com.example.phamarcy_server.dto.SyncActivityResponse;
import com.example.phamarcy_server.service.AdminReportingService;
import com.example.phamarcy_server.util.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.ADMIN)
@Tag(name = "Administration", description = "Read-only reporting endpoints for administrators")
@Validated
public class AdminController {

    private final AdminReportingService adminReportingService;

    public AdminController(AdminReportingService adminReportingService) {
        this.adminReportingService = adminReportingService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get global dashboard totals", responses = {
            @ApiResponse(responseCode = "200", description = "Dashboard summary returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public AdminDashboardResponse dashboard() {
        return adminReportingService.getDashboard();
    }

    @GetMapping("/pharmacies")
    @Operation(summary = "List all registered pharmacies", description = "Returns user-facing pharmacy summaries. Pharmacy UUIDs are internal navigation identifiers and never need to be entered by a user.", responses = {
            @ApiResponse(responseCode = "200", description = "Pharmacy list returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public List<PharmacySummaryResponse> pharmacies() {
        return adminReportingService.getPharmacies();
    }

    @GetMapping("/pharmacies/{pharmacyId}")
    @Operation(summary = "Get comprehensive pharmacy details", responses = {
            @ApiResponse(responseCode = "200", description = "Pharmacy details returned"),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public PharmacyDetailsResponse pharmacyDetails(
            @Parameter(description = "Internal pharmacy ID selected from the pharmacy list") @PathVariable UUID pharmacyId
    ) {
        return adminReportingService.getPharmacyDetails(pharmacyId);
    }

    @GetMapping("/sync-activity")
    @Operation(summary = "Get recent synchronization activity across all pharmacies", responses = {
            @ApiResponse(responseCode = "200", description = "Recent sync activity returned"),
            @ApiResponse(responseCode = "400", description = "Limit is outside the supported range", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public List<SyncActivityResponse> syncActivity(
            @Parameter(description = "Maximum records to return, from 1 through 100")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return adminReportingService.getSyncActivity(limit);
    }

    @GetMapping("/pharmacies/{pharmacyId}/sync-activity")
    @Operation(summary = "Get recent synchronization activity for a pharmacy", responses = {
            @ApiResponse(responseCode = "200", description = "Recent pharmacy sync activity returned"),
            @ApiResponse(responseCode = "400", description = "Limit is outside the supported range", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public List<SyncActivityResponse> pharmacySyncActivity(
            @Parameter(description = "Internal pharmacy ID selected from the pharmacy list") @PathVariable UUID pharmacyId,
            @Parameter(description = "Maximum records to return, from 1 through 100")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return adminReportingService.getSyncActivity(pharmacyId, limit);
    }

    @GetMapping("/pharmacies/{pharmacyId}/inventory")
    @Operation(summary = "Get inventory records for a pharmacy", responses = {
            @ApiResponse(responseCode = "200", description = "Inventory records returned"),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public List<InventoryResponse> pharmacyInventory(
            @Parameter(description = "Internal pharmacy ID selected from the pharmacy list") @PathVariable UUID pharmacyId
    ) {
        return adminReportingService.getInventory(pharmacyId);
    }

    @GetMapping("/pharmacies/{pharmacyId}/sales")
    @Operation(summary = "Get sales records for a pharmacy", responses = {
            @ApiResponse(responseCode = "200", description = "Sales records returned"),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public List<SaleResponse> pharmacySales(
            @Parameter(description = "Internal pharmacy ID selected from the pharmacy list") @PathVariable UUID pharmacyId
    ) {
        return adminReportingService.getSales(pharmacyId);
    }

    @GetMapping("/pharmacies/{pharmacyId}/sales/{saleId}")
    @Operation(
            summary = "Get sale details for a pharmacy",
            description = "Returns an active sale only when it belongs to the requested pharmacy.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sale details returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SaleDetailsResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid pharmacy or sale UUID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "404", description = "Pharmacy or scoped sale not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public SaleDetailsResponse pharmacySaleDetails(
            @Parameter(description = "Internal pharmacy ID selected from the pharmacy list") @PathVariable UUID pharmacyId,
            @Parameter(description = "Sale ID within the selected pharmacy") @PathVariable UUID saleId
    ) {
        return adminReportingService.getSaleDetails(pharmacyId, saleId);
    }

    @GetMapping("/pharmacies/{pharmacyId}/dashboard")
    @Operation(summary = "Get dashboard totals for a pharmacy", responses = {
            @ApiResponse(responseCode = "200", description = "Pharmacy dashboard returned"),
            @ApiResponse(responseCode = "404", description = "Pharmacy not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    public PharmacyDashboardResponse pharmacyDashboard(
            @Parameter(description = "Internal pharmacy ID selected from the pharmacy list") @PathVariable UUID pharmacyId
    ) {
        return adminReportingService.getPharmacyDashboard(pharmacyId);
    }
}
