package com.example.phamarcy_server;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.phamarcy_server.dto.InventorySyncItem;
import com.example.phamarcy_server.dto.SaleSyncItem;
import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;
import com.example.phamarcy_server.entity.CentralInventory;
import com.example.phamarcy_server.entity.CentralSale;
import com.example.phamarcy_server.entity.Pharmacy;
import com.example.phamarcy_server.repository.CentralInventoryRepository;
import com.example.phamarcy_server.repository.CentralSaleRepository;
import com.example.phamarcy_server.repository.PharmacyRepository;
import com.example.phamarcy_server.security.AuthenticatedPharmacy;
import com.example.phamarcy_server.service.SyncService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SyncServiceIntegrationTests {

    private static final UUID PHARMACY_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID INVENTORY_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID SALE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Autowired
    private SyncService syncService;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private CentralInventoryRepository inventoryRepository;

    @Autowired
    private CentralSaleRepository saleRepository;

    private AuthenticatedPharmacy authenticatedPharmacy;

    @BeforeEach
    void setUp() {
        saleRepository.deleteAll();
        inventoryRepository.deleteAll();
        pharmacyRepository.deleteAll();

        Pharmacy pharmacy = pharmacyRepository.save(new Pharmacy(PHARMACY_ID, "Main Branch", "Downtown", "sync-token"));
        authenticatedPharmacy = AuthenticatedPharmacy.from(pharmacy);
    }

    @Test
    void synchronizeIsIdempotentAndAppliesLastWriteWins() {
        Instant initialUpdate = Instant.parse("2026-07-07T09:00:00Z");
        SyncRequest initialRequest = request(
                inventoryItem("Paracetamol", 10, "2.50", initialUpdate),
                saleItem("25.00", initialUpdate, initialUpdate)
        );

        SyncResponse inserted = syncService.synchronize(initialRequest, authenticatedPharmacy);

        assertThat(inserted.inventory().inserted()).isEqualTo(1);
        assertThat(inserted.sales().inserted()).isEqualTo(1);

        SyncResponse duplicate = syncService.synchronize(initialRequest, authenticatedPharmacy);

        assertThat(duplicate.inventory().ignored()).isEqualTo(1);
        assertThat(duplicate.sales().ignored()).isEqualTo(1);

        Instant newerUpdate = Instant.parse("2026-07-07T10:00:00Z");
        SyncRequest newerRequest = request(
                inventoryItem("Paracetamol Extra", 15, "3.00", newerUpdate),
                saleItem("30.00", initialUpdate, newerUpdate)
        );

        SyncResponse updated = syncService.synchronize(newerRequest, authenticatedPharmacy);

        assertThat(updated.inventory().updated()).isEqualTo(1);
        assertThat(updated.sales().updated()).isEqualTo(1);

        CentralInventory inventory = inventoryRepository.findById(INVENTORY_ID).orElseThrow();
        CentralSale sale = saleRepository.findById(SALE_ID).orElseThrow();

        assertThat(inventory.getProductName()).isEqualTo("Paracetamol Extra");
        assertThat(inventory.getQuantity()).isEqualTo(15);
        assertThat(inventory.getPrice()).isEqualByComparingTo("3.00");
        assertThat(sale.getTotalAmount()).isEqualByComparingTo("30.00");
    }

    private SyncRequest request(InventorySyncItem inventoryItem, SaleSyncItem saleItem) {
        return new SyncRequest(PHARMACY_ID, List.of(inventoryItem), List.of(saleItem));
    }

    private InventorySyncItem inventoryItem(String productName, int quantity, String price, Instant lastUpdatedAt) {
        return new InventorySyncItem(INVENTORY_ID, productName, quantity, new BigDecimal(price), lastUpdatedAt);
    }

    private SaleSyncItem saleItem(String totalAmount, Instant createdAt, Instant lastUpdatedAt) {
        return new SaleSyncItem(SALE_ID, new BigDecimal(totalAmount), createdAt, lastUpdatedAt);
    }
}