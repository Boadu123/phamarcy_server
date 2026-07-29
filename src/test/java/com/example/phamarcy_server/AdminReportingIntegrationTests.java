package com.example.phamarcy_server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.phamarcy_server.repository.AppSettingRepository;
import com.example.phamarcy_server.repository.BatchRepository;
import com.example.phamarcy_server.repository.CentralInventoryRepository;
import com.example.phamarcy_server.repository.CentralSaleRepository;
import com.example.phamarcy_server.repository.PharmacyRepository;
import com.example.phamarcy_server.repository.ProductRepository;
import com.example.phamarcy_server.repository.SaleItemRepository;
import com.example.phamarcy_server.repository.SaleRepository;
import com.example.phamarcy_server.repository.SyncActivityRepository;
import com.example.phamarcy_server.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminReportingIntegrationTests {

    private static final String PHARMACY_ID = "10000000-0000-0000-0000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SyncActivityRepository syncActivityRepository;
    @Autowired
    private SaleItemRepository saleItemRepository;
    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private BatchRepository batchRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private AppSettingRepository appSettingRepository;
    @Autowired
    private CentralInventoryRepository centralInventoryRepository;
    @Autowired
    private CentralSaleRepository centralSaleRepository;
    @Autowired
    private PharmacyRepository pharmacyRepository;

    @BeforeEach
    void setUp() {
        syncActivityRepository.deleteAll();
        saleItemRepository.deleteAll();
        saleRepository.deleteAll();
        batchRepository.deleteAll();
        productRepository.deleteAll();
        userAccountRepository.deleteAll();
        appSettingRepository.deleteAll();
        centralInventoryRepository.deleteAll();
        centralSaleRepository.deleteAll();
        pharmacyRepository.deleteAll();
    }

    @Test
    void dashboardAndPharmacyNavigationUseDetailedSynchronizedRecords() throws Exception {
        synchronizeRepresentativePayload();

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_pharmacies").value(1))
                .andExpect(jsonPath("$.pharmacies_with_successful_sync").value(1))
                .andExpect(jsonPath("$.total_inventory_records").value(1))
                .andExpect(jsonPath("$.total_units_in_stock").value(120))
                .andExpect(jsonPath("$.total_inventory_value").value(90.0))
                .andExpect(jsonPath("$.total_sales_count").value(1))
                .andExpect(jsonPath("$.total_sales_amount").value(25.0))
                .andExpect(jsonPath("$.successful_syncs").value(1))
                .andExpect(jsonPath("$.failed_syncs").value(0))
                .andExpect(jsonPath("$.latest_sync.status").value("SUCCESSFUL"))
                .andExpect(jsonPath("$.latest_sync.records_received").value(6))
                .andExpect(jsonPath("$.latest_sync.inventory_records_applied").value(2))
                .andExpect(jsonPath("$.latest_sync.sales_records_applied").value(1));

        mockMvc.perform(get("/api/v1/admin/pharmacies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pharmacy_id").value(PHARMACY_ID))
                .andExpect(jsonPath("$[0].sync_status").value("SUCCESSFUL"))
                .andExpect(jsonPath("$[0].total_inventory_records").value(1))
                .andExpect(jsonPath("$[0].total_sales_count").value(1));

        mockMvc.perform(get("/api/v1/admin/pharmacies/{pharmacyId}", PHARMACY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pharmacy_id").value(PHARMACY_ID))
                .andExpect(jsonPath("$.inventory.total_records").value(1))
                .andExpect(jsonPath("$.inventory.total_units_in_stock").value(120))
                .andExpect(jsonPath("$.sales.total_transactions").value(1))
                .andExpect(jsonPath("$.latest_sync.status").value("SUCCESSFUL"))
                .andExpect(jsonPath("$.recent_activity[0].status").value("SUCCESSFUL"));

        mockMvc.perform(get("/api/v1/admin/pharmacies/{pharmacyId}/inventory", PHARMACY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].product_name").value("Paracetamol"))
                .andExpect(jsonPath("$[0].stock_reference").value("STK-20260728-A1B2C3D4E5F6"))
                .andExpect(jsonPath("$[0].inventory_value").value(90.0));

        mockMvc.perform(get("/api/v1/admin/pharmacies/{pharmacyId}/sales", PHARMACY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("cashier"))
                .andExpect(jsonPath("$[0].total_amount").value(25.0))
                .andExpect(jsonPath("$[0].item_count").value(1));

        mockMvc.perform(get("/api/v1/admin/sync-activity").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("SUCCESSFUL"));

        mockMvc.perform(get("/api/v1/admin/pharmacies/{pharmacyId}/sync-activity", PHARMACY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESSFUL"));
    }

    @Test
    void failedBusinessSyncIsPersistedWithoutPartiallyChangingOperationalData() throws Exception {
        synchronizeRepresentativePayload();

        String invalidRelationshipPayload = """
                {
                  "pharmacyId": "10000000-0000-0000-0000-000000000001",
                  "records": {
                    "batches": [{
                      "id": "40000000-0000-0000-0000-000000000002",
                      "product_id": "30000000-0000-0000-0000-000000000099",
                      "stock_reference": "STK-INVALID-RELATIONSHIP",
                      "batch_number": "UNKNOWN",
                      "quantity": 10,
                      "cost_price": 1.00,
                      "selling_price": 2.00,
                      "expiry_date": "2028-04-30",
                      "sync_status": "PENDING",
                      "last_updated_at": "2026-07-28T11:00:00Z"
                    }]
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRelationshipPayload))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_inventory_records").value(1))
                .andExpect(jsonPath("$.successful_syncs").value(1))
                .andExpect(jsonPath("$.failed_syncs").value(1))
                .andExpect(jsonPath("$.latest_sync.status").value("FAILED"))
                .andExpect(jsonPath("$.latest_sync.records_received").value(1))
                .andExpect(jsonPath("$.latest_sync.inventory_records_applied").value(0));
    }

    @Test
    void reportingValidationAndNotFoundErrorsAreFrontendFriendly() throws Exception {
        mockMvc.perform(get("/api/v1/admin/sync-activity").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        mockMvc.perform(get("/api/v1/admin/pharmacies/{pharmacyId}", PHARMACY_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/admin/pharmacies/" + PHARMACY_ID));
    }

    @Test
    void localFrontendOriginCanCallTheVersionedApi() throws Exception {
        mockMvc.perform(options("/api/v1/admin/dashboard")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void openApiPublishesTheImplementedVersionedMonitoringContract() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/admin/dashboard'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/pharmacies'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/pharmacies/{pharmacyId}'].get.responses['404'].content['application/json'].schema").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/sync-activity'].get.parameters[0].schema.maximum").value(100))
                .andExpect(jsonPath("$.paths['/api/v1/admin/pharmacies/{pharmacyId}/sync-activity'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/sync'].post.responses['409'].content['application/json'].schema").exists())
                .andExpect(jsonPath("$.components.schemas.AdminDashboardResponse.properties.total_pharmacies").exists())
                .andExpect(jsonPath("$.components.schemas.SyncActivityResponse.properties.inventory_records_applied").exists())
                .andExpect(jsonPath("$.components.schemas.SyncResponse.properties.pharmacy_id").exists());
    }

    private void synchronizeRepresentativePayload() throws Exception {
        String payload = """
                {
                  "pharmacyId": "10000000-0000-0000-0000-000000000001",
                  "records": {
                    "users": [{
                      "id": "20000000-0000-0000-0000-000000000001",
                      "username": "cashier",
                      "password_hash": "hash-value",
                      "role": "USER",
                      "created_at": "2026-07-28T09:00:00Z",
                      "sync_status": "PENDING",
                      "last_updated_at": "2026-07-28T09:00:00Z"
                    }],
                    "products": [{
                      "id": "30000000-0000-0000-0000-000000000001",
                      "name": "Paracetamol",
                      "description": "Pain relief",
                      "category": "Medicine",
                      "reorder_level": 10,
                      "created_at": "2026-07-28T09:00:00Z",
                      "sync_status": "PENDING",
                      "last_updated_at": "2026-07-28T09:00:00Z"
                    }],
                    "batches": [{
                      "id": "40000000-0000-0000-0000-000000000001",
                      "product_id": "30000000-0000-0000-0000-000000000001",
                      "stock_reference": "STK-20260728-A1B2C3D4E5F6",
                      "batch_number": "PCM500-26A041",
                      "quantity": 120,
                      "cost_price": 0.35,
                      "selling_price": 0.75,
                      "expiry_date": "2028-04-30",
                      "sync_status": "PENDING",
                      "last_updated_at": "2026-07-28T09:00:00Z"
                    }],
                    "sales": [{
                      "id": "50000000-0000-0000-0000-000000000001",
                      "user_id": "20000000-0000-0000-0000-000000000001",
                      "sale_date": "2026-07-28T10:00:00Z",
                      "total_amount": 25.00,
                      "sync_status": "PENDING",
                      "last_updated_at": "2026-07-28T10:00:00Z"
                    }],
                    "sale_items": [{
                      "id": "60000000-0000-0000-0000-000000000001",
                      "sale_id": "50000000-0000-0000-0000-000000000001",
                      "batch_id": "40000000-0000-0000-0000-000000000001",
                      "product_name": "Paracetamol",
                      "batch_number": "PCM500-26A041",
                      "quantity_sold": 2,
                      "unit_price": 0.75,
                      "sync_status": "PENDING",
                      "last_updated_at": "2026-07-28T10:00:00Z"
                    }],
                    "app_settings": [{
                      "id": "70000000-0000-0000-0000-000000000001",
                      "setting_key": "receipt_footer",
                      "setting_value": "Thank you",
                      "sync_status": "PENDING",
                      "last_updated_at": "2026-07-28T09:00:00Z"
                    }]
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/sync").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());
    }
}
