package com.example.phamarcy_server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.phamarcy_server.entity.Batch;
import com.example.phamarcy_server.entity.Pharmacy;
import com.example.phamarcy_server.entity.Product;
import com.example.phamarcy_server.entity.Sale;
import com.example.phamarcy_server.entity.SaleItem;
import com.example.phamarcy_server.entity.UserAccount;
import com.example.phamarcy_server.repository.BatchRepository;
import com.example.phamarcy_server.repository.PharmacyRepository;
import com.example.phamarcy_server.repository.ProductRepository;
import com.example.phamarcy_server.repository.SaleItemRepository;
import com.example.phamarcy_server.repository.SaleRepository;
import com.example.phamarcy_server.repository.UserAccountRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SaleDetailsIntegrationTests {

    private static final UUID PHARMACY_A_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID PHARMACY_B_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID USER_A_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID USER_B_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID PRODUCT_A_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_B_ID = UUID.fromString("30000000-0000-0000-0000-000000000002");
    private static final UUID BATCH_A_ID = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID BATCH_B_ID = UUID.fromString("40000000-0000-0000-0000-000000000002");
    private static final UUID SALE_A_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID EMPTY_SALE_ID = UUID.fromString("50000000-0000-0000-0000-000000000002");
    private static final UUID SALE_B_ID = UUID.fromString("50000000-0000-0000-0000-000000000003");
    private static final UUID UNKNOWN_PHARMACY_ID = UUID.fromString("10000000-0000-0000-0000-000000000099");
    private static final UUID UNKNOWN_SALE_ID = UUID.fromString("50000000-0000-0000-0000-000000000099");
    private static final UUID ITEM_A_ID = UUID.fromString("60000000-0000-0000-0000-000000000001");
    private static final UUID ITEM_B_ID = UUID.fromString("60000000-0000-0000-0000-000000000002");
    private static final Instant CREATED_AT = Instant.parse("2026-08-04T09:00:00Z");
    private static final Instant SALE_DATE = Instant.parse("2026-08-04T09:20:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-08-04T09:30:00Z");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
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

    @BeforeEach
    void setUp() {
        List.of(
                "sync_activities",
                "sale_items",
                "sales",
                "batches",
                "products",
                "users",
                "app_settings",
                "central_inventory",
                "central_sales",
                "pharmacies"
        ).forEach(table -> jdbcTemplate.update("delete from " + table));
        seedSales();
    }

    @Test
    void returnsCompleteScopedSaleWithAllItemsWithoutCredentials() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/admin/pharmacies/{pharmacyId}/sales/{saleId}",
                        PHARMACY_A_ID,
                        SALE_A_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SALE_A_ID.toString()))
                .andExpect(jsonPath("$.pharmacy_id").value(PHARMACY_A_ID.toString()))
                .andExpect(jsonPath("$.pharmacy_name").value("Alpha Pharmacy"))
                .andExpect(jsonPath("$.location").value("Reykjavik"))
                .andExpect(jsonPath("$.user_id").value(USER_A_ID.toString()))
                .andExpect(jsonPath("$.username").value("cashier-a"))
                .andExpect(jsonPath("$.total_amount").value(48.25))
                .andExpect(jsonPath("$.sale_date").value(SALE_DATE.toString()))
                .andExpect(jsonPath("$.item_count").value(2))
                .andExpect(jsonPath("$.created_at").value(CREATED_AT.toString()))
                .andExpect(jsonPath("$.last_updated_at").value(UPDATED_AT.toString()))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].id").value(ITEM_A_ID.toString()))
                .andExpect(jsonPath("$.items[0].product_id").value(PRODUCT_A_ID.toString()))
                .andExpect(jsonPath("$.items[0].product_name").value("Paracetamol"))
                .andExpect(jsonPath("$.items[0].batch_id").value(BATCH_A_ID.toString()))
                .andExpect(jsonPath("$.items[0].stock_reference").value("STK-PARA-001"))
                .andExpect(jsonPath("$.items[0].batch_number").value("PARA-26A"))
                .andExpect(jsonPath("$.items[0].quantity_sold").value(2))
                .andExpect(jsonPath("$.items[0].unit_price").value(12.5))
                .andExpect(jsonPath("$.items[0].subtotal").value(25.0))
                .andExpect(jsonPath("$.items[1].id").value(ITEM_B_ID.toString()))
                .andExpect(jsonPath("$.items[1].product_id").value(PRODUCT_B_ID.toString()))
                .andExpect(jsonPath("$.items[1].quantity_sold").value(3))
                .andExpect(jsonPath("$.items[1].unit_price").value(7.75))
                .andExpect(jsonPath("$.items[1].subtotal").value(23.25))
                .andExpect(jsonPath("$.api_token").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist())
                .andExpect(jsonPath("$.items[0].cost_price").doesNotExist())
                .andExpect(jsonPath("$.items[0].deleted").doesNotExist());
    }

    @Test
    void returnsAnEmptyItemsArrayWhenTheSaleHasNoItems() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/admin/pharmacies/{pharmacyId}/sales/{saleId}",
                        PHARMACY_A_ID,
                        EMPTY_SALE_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EMPTY_SALE_ID.toString()))
                .andExpect(jsonPath("$.item_count").value(0))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void returnsNotFoundWhenTheSaleDoesNotExist() throws Exception {
        String path = "/api/v1/admin/pharmacies/" + PHARMACY_A_ID + "/sales/" + UNKNOWN_SALE_ID;

        mockMvc.perform(get(path))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Sale not found for pharmacy " + PHARMACY_A_ID + ": " + UNKNOWN_SALE_ID
                ))
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.violations").isEmpty());
    }

    @Test
    void returnsNotFoundWhenThePharmacyDoesNotExist() throws Exception {
        String path = "/api/v1/admin/pharmacies/" + UNKNOWN_PHARMACY_ID + "/sales/" + SALE_A_ID;

        mockMvc.perform(get(path))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Pharmacy not found: " + UNKNOWN_PHARMACY_ID))
                .andExpect(jsonPath("$.path").value(path));
    }

    @Test
    void doesNotExposeASaleThatBelongsToAnotherPharmacy() throws Exception {
        String path = "/api/v1/admin/pharmacies/" + PHARMACY_A_ID + "/sales/" + SALE_B_ID;

        mockMvc.perform(get(path))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(
                        "Sale not found for pharmacy " + PHARMACY_A_ID + ": " + SALE_B_ID
                ))
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.total_amount").doesNotExist())
                .andExpect(jsonPath("$.items").doesNotExist());
    }

    @Test
    void returnsBadRequestForAnInvalidPharmacyUuid() throws Exception {
        String path = "/api/v1/admin/pharmacies/not-a-uuid/sales/" + SALE_A_ID;

        mockMvc.perform(get(path))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.violations[0].field").value("pharmacyId"))
                .andExpect(jsonPath("$.violations[0].message").value("must be a valid UUID"));
    }

    @Test
    void returnsBadRequestForAnInvalidSaleUuid() throws Exception {
        String path = "/api/v1/admin/pharmacies/" + PHARMACY_A_ID + "/sales/not-a-uuid";

        mockMvc.perform(get(path))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.violations[0].field").value("saleId"))
                .andExpect(jsonPath("$.violations[0].message").value("must be a valid UUID"));
    }

    @Test
    void publishesTheSaleDetailsContractInOpenApi() throws Exception {
        String operation = "$.paths['/api/v1/admin/pharmacies/{pharmacyId}/sales/{saleId}'].get";

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(operation + ".parameters[0].name").value("pharmacyId"))
                .andExpect(jsonPath(operation + ".parameters[0].required").value(true))
                .andExpect(jsonPath(operation + ".parameters[1].name").value("saleId"))
                .andExpect(jsonPath(operation + ".parameters[1].required").value(true))
                .andExpect(jsonPath(operation + ".responses['200'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/SaleDetailsResponse"))
                .andExpect(jsonPath(operation + ".responses['400'].content['application/json'].schema").exists())
                .andExpect(jsonPath(operation + ".responses['404'].content['application/json'].schema").exists())
                .andExpect(jsonPath(operation + ".responses['500'].content['application/json'].schema").exists())
                .andExpect(jsonPath("$.components.schemas.SaleDetailsResponse.properties.items").exists())
                .andExpect(jsonPath("$.components.schemas.SaleItemResponse.properties.subtotal").exists());
    }

    @Test
    void unmappedAdminResourcesUseTheStandardNotFoundResponse() throws Exception {
        String path = "/api/v1/admin/pharmacies/" + PHARMACY_A_ID + "/sales/" + SALE_A_ID + "/unknown";

        mockMvc.perform(get(path))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.path").value(path));
    }

    private void seedSales() {
        Pharmacy pharmacyA = pharmacyRepository.save(
                new Pharmacy(PHARMACY_A_ID, "Alpha Pharmacy", "Reykjavik", "alpha-token")
        );
        Pharmacy pharmacyB = pharmacyRepository.save(
                new Pharmacy(PHARMACY_B_ID, "Beta Pharmacy", "Akureyri", "beta-token")
        );
        UserAccount userA = userAccountRepository.save(
                new UserAccount(USER_A_ID, pharmacyA, "cashier-a", "hash-a", "CASHIER", CREATED_AT, UPDATED_AT)
        );
        UserAccount userB = userAccountRepository.save(
                new UserAccount(USER_B_ID, pharmacyB, "cashier-b", "hash-b", "CASHIER", CREATED_AT, UPDATED_AT)
        );
        Product productA = productRepository.save(
                new Product(PRODUCT_A_ID, pharmacyA, "Paracetamol", "Pain relief", "Medicine", 10, CREATED_AT, UPDATED_AT)
        );
        Product productB = productRepository.save(
                new Product(PRODUCT_B_ID, pharmacyA, "ORS", "Rehydration salts", "Medicine", 5, CREATED_AT, UPDATED_AT)
        );
        Batch batchA = batchRepository.save(
                new Batch(BATCH_A_ID, pharmacyA, productA, "STK-PARA-001", "PARA-26A", 100,
                        new BigDecimal("8.00"), new BigDecimal("12.50"), LocalDate.parse("2028-04-30"), CREATED_AT, UPDATED_AT)
        );
        Batch batchB = batchRepository.save(
                new Batch(BATCH_B_ID, pharmacyA, productB, "STK-ORS-002", "ORS-26B", 80,
                        new BigDecimal("4.00"), new BigDecimal("7.75"), LocalDate.parse("2028-06-30"), CREATED_AT, UPDATED_AT)
        );
        Sale saleA = saleRepository.save(
                new Sale(SALE_A_ID, pharmacyA, userA, SALE_DATE, new BigDecimal("48.25"), CREATED_AT, UPDATED_AT)
        );
        saleRepository.save(
                new Sale(EMPTY_SALE_ID, pharmacyA, userA, SALE_DATE.plusSeconds(60), BigDecimal.ZERO, CREATED_AT, UPDATED_AT)
        );
        saleRepository.save(
                new Sale(SALE_B_ID, pharmacyB, userB, SALE_DATE, new BigDecimal("9.00"), CREATED_AT, UPDATED_AT)
        );
        saleItemRepository.save(
                new SaleItem(ITEM_A_ID, pharmacyA, saleA, batchA, "Paracetamol", "PARA-26A", 2,
                        new BigDecimal("12.50"), CREATED_AT, UPDATED_AT)
        );
        saleItemRepository.save(
                new SaleItem(ITEM_B_ID, pharmacyA, saleA, batchB, "ORS", "ORS-26B", 3,
                        new BigDecimal("7.75"), CREATED_AT.plusSeconds(1), UPDATED_AT)
        );
    }
}
