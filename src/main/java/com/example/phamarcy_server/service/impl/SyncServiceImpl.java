package com.example.phamarcy_server.service.impl;

import com.example.phamarcy_server.dto.AppSettingSyncItem;
import com.example.phamarcy_server.dto.BatchSyncItem;
import com.example.phamarcy_server.dto.ProductSyncItem;
import com.example.phamarcy_server.dto.SaleItemSyncItem;
import com.example.phamarcy_server.dto.SaleSyncItem;
import com.example.phamarcy_server.dto.SyncEntityResult;
import com.example.phamarcy_server.dto.SyncRecords;
import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;
import com.example.phamarcy_server.dto.UserSyncItem;
import com.example.phamarcy_server.entity.AppSetting;
import com.example.phamarcy_server.entity.Batch;
import com.example.phamarcy_server.entity.Pharmacy;
import com.example.phamarcy_server.entity.Product;
import com.example.phamarcy_server.entity.Sale;
import com.example.phamarcy_server.entity.SaleItem;
import com.example.phamarcy_server.entity.UserAccount;
import com.example.phamarcy_server.exception.DuplicateStockReferenceException;
import com.example.phamarcy_server.exception.DuplicateSyncRecordException;
import com.example.phamarcy_server.exception.MissingSyncRelationshipException;
import com.example.phamarcy_server.exception.SyncOwnershipConflictException;
import com.example.phamarcy_server.repository.AppSettingRepository;
import com.example.phamarcy_server.repository.BatchRepository;
import com.example.phamarcy_server.repository.PharmacyRepository;
import com.example.phamarcy_server.repository.ProductRepository;
import com.example.phamarcy_server.repository.SaleItemRepository;
import com.example.phamarcy_server.repository.SaleRepository;
import com.example.phamarcy_server.repository.UserAccountRepository;
import com.example.phamarcy_server.service.SyncService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncServiceImpl implements SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncServiceImpl.class);

    private final PharmacyRepository pharmacyRepository;
    private final UserAccountRepository userAccountRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final AppSettingRepository appSettingRepository;

    public SyncServiceImpl(
            PharmacyRepository pharmacyRepository,
            UserAccountRepository userAccountRepository,
            ProductRepository productRepository,
            BatchRepository batchRepository,
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository,
            AppSettingRepository appSettingRepository
    ) {
        this.pharmacyRepository = pharmacyRepository;
        this.userAccountRepository = userAccountRepository;
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.appSettingRepository = appSettingRepository;
    }

    @Override
    @Transactional
    public SyncResponse synchronize(SyncRequest request) {
        Pharmacy pharmacy = findOrCreatePharmacy(request.pharmacyId());
        SyncRecords records = request.records();

        assertUniqueIds(records.users(), UserSyncItem::id, "users");
        assertUniqueIds(records.products(), ProductSyncItem::id, "products");
        assertUniqueIds(records.batches(), BatchSyncItem::id, "batches");
        assertUniqueIds(records.sales(), SaleSyncItem::id, "sales");
        assertUniqueIds(records.saleItems(), SaleItemSyncItem::id, "sale_items");
        assertUniqueIds(records.appSettings(), AppSettingSyncItem::id, "app_settings");
        assertUniqueStockReferences(records.batches(), pharmacy.getId());

        SyncOutcome<UserAccount> users = synchronizeUsers(records.users(), pharmacy);
        SyncOutcome<Product> products = synchronizeProducts(records.products(), pharmacy);
        SyncOutcome<Batch> batches = synchronizeBatches(records.batches(), pharmacy, products.entitiesById());
        SyncOutcome<Sale> sales = synchronizeSales(records.sales(), pharmacy, users.entitiesById());
        SyncOutcome<SaleItem> saleItems = synchronizeSaleItems(records.saleItems(), pharmacy, sales.entitiesById(), batches.entitiesById());
        SyncOutcome<AppSetting> appSettings = synchronizeAppSettings(records.appSettings(), pharmacy);

        log.info(
                "Synchronized pharmacy {}: users inserted={}, updated={}, ignored={}; products inserted={}, updated={}, ignored={}; batches inserted={}, updated={}, ignored={}; sales inserted={}, updated={}, ignored={}; sale_items inserted={}, updated={}, ignored={}; app_settings inserted={}, updated={}, ignored={}",
                pharmacy.getId(),
                users.result().inserted(), users.result().updated(), users.result().ignored(),
                products.result().inserted(), products.result().updated(), products.result().ignored(),
                batches.result().inserted(), batches.result().updated(), batches.result().ignored(),
                sales.result().inserted(), sales.result().updated(), sales.result().ignored(),
                saleItems.result().inserted(), saleItems.result().updated(), saleItems.result().ignored(),
                appSettings.result().inserted(), appSettings.result().updated(), appSettings.result().ignored()
        );

        return new SyncResponse(
                pharmacy.getId(),
                users.result(),
                products.result(),
                batches.result(),
                sales.result(),
                saleItems.result(),
                appSettings.result()
        );
    }

    private SyncOutcome<UserAccount> synchronizeUsers(List<UserSyncItem> incomingItems, Pharmacy pharmacy) {
        Map<UUID, UserAccount> entitiesById = userAccountRepository.findAllById(idsOf(incomingItems, UserSyncItem::id))
                .stream()
                .collect(Collectors.toMap(UserAccount::getId, Function.identity()));

        List<UserAccount> inserts = new ArrayList<>();
        int updated = 0;
        int ignored = 0;

        for (UserSyncItem incoming : incomingItems) {
            UserAccount existing = entitiesById.get(incoming.id());
            if (existing == null) {
                UserAccount insert = new UserAccount(
                        incoming.id(),
                        pharmacy,
                        incoming.username(),
                        incoming.passwordHash(),
                        incoming.role(),
                        incoming.createdAt(),
                        incoming.lastUpdatedAt()
                );
                inserts.add(insert);
                entitiesById.put(insert.getId(), insert);
                continue;
            }

            verifyOwnership("User", existing.getId(), existing.getPharmacy().getId(), pharmacy.getId());
            if (isNewer(incoming.lastUpdatedAt(), existing.getLastUpdatedAt())) {
                existing.update(incoming.username(), incoming.passwordHash(), incoming.role(), incoming.createdAt(), incoming.lastUpdatedAt());
                updated++;
            } else {
                ignored++;
            }
        }

        userAccountRepository.saveAll(inserts);
        return new SyncOutcome<>(new SyncEntityResult(inserts.size(), updated, ignored), entitiesById);
    }

    private SyncOutcome<Product> synchronizeProducts(List<ProductSyncItem> incomingItems, Pharmacy pharmacy) {
        Map<UUID, Product> entitiesById = productRepository.findAllById(idsOf(incomingItems, ProductSyncItem::id))
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Product> inserts = new ArrayList<>();
        int updated = 0;
        int ignored = 0;

        for (ProductSyncItem incoming : incomingItems) {
            Product existing = entitiesById.get(incoming.id());
            if (existing == null) {
                Product insert = new Product(
                        incoming.id(),
                        pharmacy,
                        incoming.name(),
                        incoming.description(),
                        incoming.category(),
                        incoming.reorderLevel(),
                        incoming.createdAt(),
                        incoming.lastUpdatedAt()
                );
                inserts.add(insert);
                entitiesById.put(insert.getId(), insert);
                continue;
            }

            verifyOwnership("Product", existing.getId(), existing.getPharmacy().getId(), pharmacy.getId());
            if (isNewer(incoming.lastUpdatedAt(), existing.getLastUpdatedAt())) {
                existing.update(incoming.name(), incoming.description(), incoming.category(), incoming.reorderLevel(), incoming.createdAt(), incoming.lastUpdatedAt());
                updated++;
            } else {
                ignored++;
            }
        }

        productRepository.saveAll(inserts);
        return new SyncOutcome<>(new SyncEntityResult(inserts.size(), updated, ignored), entitiesById);
    }

    private SyncOutcome<Batch> synchronizeBatches(List<BatchSyncItem> incomingItems, Pharmacy pharmacy, Map<UUID, Product> productsById) {
        Map<UUID, Batch> entitiesById = batchRepository.findAllById(idsOf(incomingItems, BatchSyncItem::id))
                .stream()
                .collect(Collectors.toMap(Batch::getId, Function.identity()));

        List<Batch> inserts = new ArrayList<>();
        int updated = 0;
        int ignored = 0;

        for (BatchSyncItem incoming : incomingItems) {
            Product product = resolveProduct(incoming.id(), incoming.productId(), productsById, pharmacy.getId());
            verifyStockReferenceAvailable(pharmacy.getId(), incoming.stockReference(), incoming.id());

            Batch existing = entitiesById.get(incoming.id());
            if (existing == null) {
                Batch insert = new Batch(
                        incoming.id(),
                        pharmacy,
                        product,
                        incoming.stockReference(),
                        incoming.batchNumber(),
                        incoming.quantity(),
                        incoming.costPrice(),
                        incoming.sellingPrice(),
                        incoming.expiryDate(),
                        incoming.lastUpdatedAt(),
                        incoming.lastUpdatedAt()
                );
                inserts.add(insert);
                entitiesById.put(insert.getId(), insert);
                continue;
            }

            verifyOwnership("Batch", existing.getId(), existing.getPharmacy().getId(), pharmacy.getId());
            if (isNewer(incoming.lastUpdatedAt(), existing.getLastUpdatedAt())) {
                existing.update(product, incoming.stockReference(), incoming.batchNumber(), incoming.quantity(), incoming.costPrice(), incoming.sellingPrice(), incoming.expiryDate(), incoming.lastUpdatedAt());
                updated++;
            } else {
                ignored++;
            }
        }

        batchRepository.saveAll(inserts);
        return new SyncOutcome<>(new SyncEntityResult(inserts.size(), updated, ignored), entitiesById);
    }

    private SyncOutcome<Sale> synchronizeSales(List<SaleSyncItem> incomingItems, Pharmacy pharmacy, Map<UUID, UserAccount> usersById) {
        Map<UUID, Sale> entitiesById = saleRepository.findAllById(idsOf(incomingItems, SaleSyncItem::id))
                .stream()
                .collect(Collectors.toMap(Sale::getId, Function.identity()));

        List<Sale> inserts = new ArrayList<>();
        int updated = 0;
        int ignored = 0;

        for (SaleSyncItem incoming : incomingItems) {
            UserAccount user = resolveUser(incoming.id(), incoming.userId(), usersById, pharmacy.getId());
            Sale existing = entitiesById.get(incoming.id());
            if (existing == null) {
                Sale insert = new Sale(
                        incoming.id(),
                        pharmacy,
                        user,
                        incoming.saleDate(),
                        incoming.totalAmount(),
                        incoming.saleDate(),
                        incoming.lastUpdatedAt()
                );
                inserts.add(insert);
                entitiesById.put(insert.getId(), insert);
                continue;
            }

            verifyOwnership("Sale", existing.getId(), existing.getPharmacy().getId(), pharmacy.getId());
            if (isNewer(incoming.lastUpdatedAt(), existing.getLastUpdatedAt())) {
                existing.update(user, incoming.saleDate(), incoming.totalAmount(), incoming.lastUpdatedAt());
                updated++;
            } else {
                ignored++;
            }
        }

        saleRepository.saveAll(inserts);
        return new SyncOutcome<>(new SyncEntityResult(inserts.size(), updated, ignored), entitiesById);
    }

    private SyncOutcome<SaleItem> synchronizeSaleItems(List<SaleItemSyncItem> incomingItems, Pharmacy pharmacy, Map<UUID, Sale> salesById, Map<UUID, Batch> batchesById) {
        Map<UUID, SaleItem> entitiesById = saleItemRepository.findAllById(idsOf(incomingItems, SaleItemSyncItem::id))
                .stream()
                .collect(Collectors.toMap(SaleItem::getId, Function.identity()));

        List<SaleItem> inserts = new ArrayList<>();
        int updated = 0;
        int ignored = 0;

        for (SaleItemSyncItem incoming : incomingItems) {
            Sale sale = resolveSale(incoming.id(), incoming.saleId(), salesById, pharmacy.getId());
            Batch batch = resolveBatch(incoming.id(), incoming.batchId(), batchesById, pharmacy.getId());
            SaleItem existing = entitiesById.get(incoming.id());
            if (existing == null) {
                SaleItem insert = new SaleItem(
                        incoming.id(),
                        pharmacy,
                        sale,
                        batch,
                        incoming.productName(),
                        incoming.batchNumber(),
                        incoming.quantitySold(),
                        incoming.unitPrice(),
                        incoming.lastUpdatedAt(),
                        incoming.lastUpdatedAt()
                );
                inserts.add(insert);
                entitiesById.put(insert.getId(), insert);
                continue;
            }

            verifyOwnership("Sale item", existing.getId(), existing.getPharmacy().getId(), pharmacy.getId());
            if (isNewer(incoming.lastUpdatedAt(), existing.getLastUpdatedAt())) {
                existing.update(sale, batch, incoming.productName(), incoming.batchNumber(), incoming.quantitySold(), incoming.unitPrice(), incoming.lastUpdatedAt());
                updated++;
            } else {
                ignored++;
            }
        }

        saleItemRepository.saveAll(inserts);
        return new SyncOutcome<>(new SyncEntityResult(inserts.size(), updated, ignored), entitiesById);
    }

    private SyncOutcome<AppSetting> synchronizeAppSettings(List<AppSettingSyncItem> incomingItems, Pharmacy pharmacy) {
        Map<UUID, AppSetting> entitiesById = appSettingRepository.findAllById(idsOf(incomingItems, AppSettingSyncItem::id))
                .stream()
                .collect(Collectors.toMap(AppSetting::getId, Function.identity()));

        List<AppSetting> inserts = new ArrayList<>();
        int updated = 0;
        int ignored = 0;

        for (AppSettingSyncItem incoming : incomingItems) {
            AppSetting existing = entitiesById.get(incoming.id());
            if (existing == null) {
                AppSetting insert = new AppSetting(
                        incoming.id(),
                        pharmacy,
                        incoming.settingKey(),
                        incoming.settingValue(),
                        incoming.lastUpdatedAt(),
                        incoming.lastUpdatedAt()
                );
                inserts.add(insert);
                entitiesById.put(insert.getId(), insert);
                continue;
            }

            verifyOwnership("App setting", existing.getId(), existing.getPharmacy().getId(), pharmacy.getId());
            if (isNewer(incoming.lastUpdatedAt(), existing.getLastUpdatedAt())) {
                existing.update(incoming.settingKey(), incoming.settingValue(), incoming.lastUpdatedAt());
                updated++;
            } else {
                ignored++;
            }
        }

        appSettingRepository.saveAll(inserts);
        return new SyncOutcome<>(new SyncEntityResult(inserts.size(), updated, ignored), entitiesById);
    }

    private Pharmacy findOrCreatePharmacy(UUID pharmacyId) {
        return pharmacyRepository.findById(pharmacyId)
                .orElseGet(() -> {
                    log.info("Creating placeholder pharmacy {} from sync payload", pharmacyId);
                    return pharmacyRepository.save(new Pharmacy(
                            pharmacyId,
                            "Imported Pharmacy " + pharmacyId,
                            "Unknown",
                            "auto-" + pharmacyId
                    ));
                });
    }

    private Product resolveProduct(UUID batchId, UUID productId, Map<UUID, Product> productsById, UUID pharmacyId) {
        Product product = productsById.get(productId);
        if (product == null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new MissingSyncRelationshipException("Batch", batchId, "product", productId));
        }
        verifyOwnership("Product", product.getId(), product.getPharmacy().getId(), pharmacyId);
        return product;
    }

    private UserAccount resolveUser(UUID saleId, UUID userId, Map<UUID, UserAccount> usersById, UUID pharmacyId) {
        UserAccount user = usersById.get(userId);
        if (user == null) {
            user = userAccountRepository.findById(userId)
                    .orElseThrow(() -> new MissingSyncRelationshipException("Sale", saleId, "user", userId));
        }
        verifyOwnership("User", user.getId(), user.getPharmacy().getId(), pharmacyId);
        return user;
    }

    private Sale resolveSale(UUID saleItemId, UUID saleId, Map<UUID, Sale> salesById, UUID pharmacyId) {
        Sale sale = salesById.get(saleId);
        if (sale == null) {
            sale = saleRepository.findById(saleId)
                    .orElseThrow(() -> new MissingSyncRelationshipException("Sale item", saleItemId, "sale", saleId));
        }
        verifyOwnership("Sale", sale.getId(), sale.getPharmacy().getId(), pharmacyId);
        return sale;
    }

    private Batch resolveBatch(UUID saleItemId, UUID batchId, Map<UUID, Batch> batchesById, UUID pharmacyId) {
        Batch batch = batchesById.get(batchId);
        if (batch == null) {
            batch = batchRepository.findById(batchId)
                    .orElseThrow(() -> new MissingSyncRelationshipException("Sale item", saleItemId, "batch", batchId));
        }
        verifyOwnership("Batch", batch.getId(), batch.getPharmacy().getId(), pharmacyId);
        return batch;
    }

    private void assertUniqueStockReferences(List<BatchSyncItem> batches, UUID pharmacyId) {
        Map<String, UUID> seen = new HashMap<>();
        for (BatchSyncItem batch : batches) {
            UUID previous = seen.putIfAbsent(batch.stockReference(), batch.id());
            if (previous != null && !previous.equals(batch.id())) {
                throw new DuplicateStockReferenceException(pharmacyId, batch.stockReference());
            }
        }
    }

    private void verifyStockReferenceAvailable(UUID pharmacyId, String stockReference, UUID batchId) {
        batchRepository.findByPharmacy_IdAndStockReference(pharmacyId, stockReference)
                .filter(existing -> !existing.getId().equals(batchId))
                .ifPresent(existing -> {
                    throw new DuplicateStockReferenceException(pharmacyId, stockReference);
                });
    }

    private <T> void assertUniqueIds(List<T> items, Function<T, UUID> idExtractor, String resourceType) {
        Set<UUID> seen = new HashSet<>();
        for (T item : items) {
            UUID id = idExtractor.apply(item);
            if (id != null && !seen.add(id)) {
                throw new DuplicateSyncRecordException(resourceType, id);
            }
        }
    }

    private <T> Set<UUID> idsOf(List<T> items, Function<T, UUID> idExtractor) {
        return items.stream()
                .map(idExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private boolean isNewer(java.time.Instant incomingLastUpdatedAt, java.time.Instant existingLastUpdatedAt) {
        return incomingLastUpdatedAt.isAfter(existingLastUpdatedAt);
    }

    private void verifyOwnership(String resourceType, UUID resourceId, UUID ownerPharmacyId, UUID expectedPharmacyId) {
        if (!ownerPharmacyId.equals(expectedPharmacyId)) {
            throw new SyncOwnershipConflictException(resourceType, resourceId);
        }
    }

    private record SyncOutcome<T>(SyncEntityResult result, Map<UUID, T> entitiesById) {
    }
}
