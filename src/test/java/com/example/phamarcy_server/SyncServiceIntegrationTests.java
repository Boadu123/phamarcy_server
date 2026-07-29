package com.example.phamarcy_server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.phamarcy_server.dto.AppSettingSyncItem;
import com.example.phamarcy_server.dto.BatchSyncItem;
import com.example.phamarcy_server.dto.ProductSyncItem;
import com.example.phamarcy_server.dto.SaleItemSyncItem;
import com.example.phamarcy_server.dto.SaleSyncItem;
import com.example.phamarcy_server.dto.SyncRecords;
import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;
import com.example.phamarcy_server.dto.SyncStatus;
import com.example.phamarcy_server.dto.UserSyncItem;
import com.example.phamarcy_server.entity.Batch;
import com.example.phamarcy_server.entity.Product;
import com.example.phamarcy_server.entity.Sale;
import com.example.phamarcy_server.entity.SaleItem;
import com.example.phamarcy_server.repository.AppSettingRepository;
import com.example.phamarcy_server.repository.BatchRepository;
import com.example.phamarcy_server.repository.PharmacyRepository;
import com.example.phamarcy_server.repository.ProductRepository;
import com.example.phamarcy_server.repository.SaleItemRepository;
import com.example.phamarcy_server.repository.SaleRepository;
import com.example.phamarcy_server.repository.UserAccountRepository;
import com.example.phamarcy_server.service.SyncService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SyncServiceIntegrationTests {

    private static final UUID PHARMACY_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID BATCH_ID = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID BATCH_TWO_ID = UUID.fromString("40000000-0000-0000-0000-000000000002");
    private static final UUID SALE_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID SALE_ITEM_ID = UUID.fromString("60000000-0000-0000-0000-000000000001");
    private static final UUID SETTING_ID = UUID.fromString("70000000-0000-0000-0000-000000000001");

    @Autowired
    private SyncService syncService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private AppSettingRepository appSettingRepository;

    @BeforeEach
    void setUp() {
        saleItemRepository.deleteAll();
        saleRepository.deleteAll();
        batchRepository.deleteAll();
        productRepository.deleteAll();
        userAccountRepository.deleteAll();
        appSettingRepository.deleteAll();
        pharmacyRepository.deleteAll();
    }

    @Test
    void healthEndpointIsOpen() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void syncEndpointAcceptsCurrentDesktopPayloadWithoutToken() throws Exception {
        String payload = """
                {
                  "pharmacyId": "10000000-0000-0000-0000-000000000001",
                  "records": {
                    "users": [{
                      "id": "20000000-0000-0000-0000-000000000001",
                      "username": "cashier",
                      "password_hash": "hash-value",
                      "role": "CASHIER",
                      "created_at": "2026-07-20T12:00:00Z",
                      "sync_status": "PENDING",
                      "last_updated_at": "2026-07-20T12:00:00Z"
                    }],
                    "products": [{
                      "id": "30000000-0000-0000-0000-000000000001",
                      "name": "Paracetamol",
                      "description": "Pain relief",
                      "category": "Medicine",
                      "reorder_level": 10,
                      "created_at": "2026-07-20T12:00:00Z",
                      "sync_status": "PENDING",
                      "last_updated_at": "2026-07-20T12:00:00Z"
                    }],
                    "batches": [{
                      "id": "40000000-0000-0000-0000-000000000001",
                      "product_id": "30000000-0000-0000-0000-000000000001",
                      "stock_reference": "STK-20260720-A1B2C3D4E5F6",
                      "batch_number": "PCM500-26A041",
                      "quantity": 120,
                      "cost_price": 0.35,
                      "selling_price": 0.75,
                      "expiry_date": "2028-04-30",
                      "sync_status": "PENDING",
                      "last_updated_at": "2026-07-20T12:00:00Z"
                    }]
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/sync").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk());

        assertThat(batchRepository.findById(BATCH_ID)).isPresent();
    }

    @Test
    void synchronizeFullRecordsIsIdempotentAndAppliesLastWriteWins() {
        Instant initialUpdate = Instant.parse("2026-07-20T12:00:00Z");
        SyncRequest initialRequest = fullRequest(initialUpdate, "Paracetamol", 120, "25.00", 2);

        SyncResponse inserted = syncService.synchronize(initialRequest);

        assertThat(inserted.users().inserted()).isEqualTo(1);
        assertThat(inserted.products().inserted()).isEqualTo(1);
        assertThat(inserted.batches().inserted()).isEqualTo(1);
        assertThat(inserted.sales().inserted()).isEqualTo(1);
        assertThat(inserted.saleItems().inserted()).isEqualTo(1);
        assertThat(inserted.appSettings().inserted()).isEqualTo(1);
        assertThat(pharmacyRepository.existsById(PHARMACY_ID)).isTrue();

        SyncResponse duplicate = syncService.synchronize(initialRequest);

        assertThat(duplicate.users().ignored()).isEqualTo(1);
        assertThat(duplicate.products().ignored()).isEqualTo(1);
        assertThat(duplicate.batches().ignored()).isEqualTo(1);
        assertThat(duplicate.sales().ignored()).isEqualTo(1);
        assertThat(duplicate.saleItems().ignored()).isEqualTo(1);
        assertThat(duplicate.appSettings().ignored()).isEqualTo(1);

        Instant newerUpdate = Instant.parse("2026-07-20T12:05:00Z");
        SyncResponse updated = syncService.synchronize(fullRequest(newerUpdate, "Paracetamol Extra", 150, "30.00", 3));

        assertThat(updated.users().updated()).isEqualTo(1);
        assertThat(updated.products().updated()).isEqualTo(1);
        assertThat(updated.batches().updated()).isEqualTo(1);
        assertThat(updated.sales().updated()).isEqualTo(1);
        assertThat(updated.saleItems().updated()).isEqualTo(1);
        assertThat(updated.appSettings().updated()).isEqualTo(1);

        Product product = productRepository.findById(PRODUCT_ID).orElseThrow();
        Batch batch = batchRepository.findById(BATCH_ID).orElseThrow();
        Sale sale = saleRepository.findById(SALE_ID).orElseThrow();
        SaleItem saleItem = saleItemRepository.findById(SALE_ITEM_ID).orElseThrow();

        assertThat(product.getName()).isEqualTo("Paracetamol Extra");
        assertThat(batch.getStockReference()).isEqualTo("STK-20260720-A1B2C3D4E5F6");
        assertThat(batch.getBatchNumber()).isEqualTo("PCM500-26A041");
        assertThat(batch.getQuantity()).isEqualTo(150);
        assertThat(sale.getTotalAmount()).isEqualByComparingTo("30.00");
        assertThat(saleItem.getQuantitySold()).isEqualTo(3);
    }

    @Test
    void synchronizeAllowsRepeatedManufacturerLotNumbersWhenStockReferencesDiffer() {
        Instant timestamp = Instant.parse("2026-07-20T12:00:00Z");
        SyncRequest request = new SyncRequest(
                PHARMACY_ID,
                new SyncRecords(
                        List.of(userItem(timestamp)),
                        List.of(productItem(timestamp, "Paracetamol", 10)),
                        List.of(
                                batchItem(BATCH_ID, "STK-20260720-A1B2C3D4E5F6", "PCM500-26A041", 120, timestamp),
                                batchItem(BATCH_TWO_ID, "STK-20260720-F6E5D4C3B2A1", "PCM500-26A041", 80, timestamp)
                        ),
                        List.of(),
                        List.of(),
                        List.of()
                )
        );

        SyncResponse response = syncService.synchronize(request);

        assertThat(response.batches().inserted()).isEqualTo(2);
        assertThat(batchRepository.findAll())
                .hasSize(2)
                .allSatisfy(batch -> assertThat(batch.getBatchNumber()).isEqualTo("PCM500-26A041"));
    }

    private SyncRequest fullRequest(Instant timestamp, String productName, int batchQuantity, String saleTotal, int quantitySold) {
        return new SyncRequest(
                PHARMACY_ID,
                new SyncRecords(
                        List.of(userItem(timestamp)),
                        List.of(productItem(timestamp, productName, 10)),
                        List.of(batchItem(BATCH_ID, "STK-20260720-A1B2C3D4E5F6", "PCM500-26A041", batchQuantity, timestamp)),
                        List.of(saleItem(saleTotal, timestamp)),
                        List.of(saleLineItem(quantitySold, timestamp)),
                        List.of(appSettingItem(timestamp))
                )
        );
    }

    private UserSyncItem userItem(Instant timestamp) {
        return new UserSyncItem(USER_ID, "cashier", "hash-value", "CASHIER", timestamp, SyncStatus.PENDING, timestamp);
    }

    private ProductSyncItem productItem(Instant timestamp, String name, int reorderLevel) {
        return new ProductSyncItem(PRODUCT_ID, name, "Pain relief", "Medicine", reorderLevel, timestamp, SyncStatus.PENDING, timestamp);
    }

    private BatchSyncItem batchItem(UUID id, String stockReference, String batchNumber, int quantity, Instant timestamp) {
        return new BatchSyncItem(
                id,
                PRODUCT_ID,
                stockReference,
                batchNumber,
                quantity,
                new BigDecimal("0.35"),
                new BigDecimal("0.75"),
                LocalDate.parse("2028-04-30"),
                SyncStatus.PENDING,
                timestamp
        );
    }

    private SaleSyncItem saleItem(String totalAmount, Instant timestamp) {
        return new SaleSyncItem(SALE_ID, USER_ID, timestamp, new BigDecimal(totalAmount), SyncStatus.PENDING, timestamp);
    }

    private SaleItemSyncItem saleLineItem(int quantitySold, Instant timestamp) {
        return new SaleItemSyncItem(
                SALE_ITEM_ID,
                SALE_ID,
                BATCH_ID,
                "Paracetamol",
                "PCM500-26A041",
                quantitySold,
                new BigDecimal("0.75"),
                SyncStatus.PENDING,
                timestamp
        );
    }

    private AppSettingSyncItem appSettingItem(Instant timestamp) {
        return new AppSettingSyncItem(SETTING_ID, "receipt_footer", "Thank you", SyncStatus.PENDING, timestamp);
    }
}
