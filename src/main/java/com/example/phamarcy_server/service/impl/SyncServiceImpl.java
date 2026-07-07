package com.example.phamarcy_server.service.impl;

import com.example.phamarcy_server.dto.InventorySyncItem;
import com.example.phamarcy_server.dto.SaleSyncItem;
import com.example.phamarcy_server.dto.SyncEntityResult;
import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;
import com.example.phamarcy_server.entity.CentralInventory;
import com.example.phamarcy_server.entity.CentralSale;
import com.example.phamarcy_server.entity.Pharmacy;
import com.example.phamarcy_server.exception.DuplicateSyncRecordException;
import com.example.phamarcy_server.exception.PharmacyMismatchException;
import com.example.phamarcy_server.exception.PharmacyNotFoundException;
import com.example.phamarcy_server.exception.SyncOwnershipConflictException;
import com.example.phamarcy_server.mapper.CentralInventoryMapper;
import com.example.phamarcy_server.mapper.CentralSaleMapper;
import com.example.phamarcy_server.repository.CentralInventoryRepository;
import com.example.phamarcy_server.repository.CentralSaleRepository;
import com.example.phamarcy_server.repository.PharmacyRepository;
import com.example.phamarcy_server.security.AuthenticatedPharmacy;
import com.example.phamarcy_server.service.SyncService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final CentralInventoryRepository inventoryRepository;
    private final CentralSaleRepository saleRepository;
    private final CentralInventoryMapper inventoryMapper;
    private final CentralSaleMapper saleMapper;

    public SyncServiceImpl(
            PharmacyRepository pharmacyRepository,
            CentralInventoryRepository inventoryRepository,
            CentralSaleRepository saleRepository,
            CentralInventoryMapper inventoryMapper,
            CentralSaleMapper saleMapper
    ) {
        this.pharmacyRepository = pharmacyRepository;
        this.inventoryRepository = inventoryRepository;
        this.saleRepository = saleRepository;
        this.inventoryMapper = inventoryMapper;
        this.saleMapper = saleMapper;
    }

    @Override
    @Transactional
    public SyncResponse synchronize(SyncRequest request, AuthenticatedPharmacy authenticatedPharmacy) {
        if (!authenticatedPharmacy.id().equals(request.pharmacyId())) {
            throw new PharmacyMismatchException();
        }

        Pharmacy pharmacy = pharmacyRepository.findById(authenticatedPharmacy.id())
                .orElseThrow(() -> new PharmacyNotFoundException(authenticatedPharmacy.id()));

        assertUniqueIds(request.inventory(), InventorySyncItem::id, "inventory");
        assertUniqueIds(request.sales(), SaleSyncItem::id, "sales");

        SyncEntityResult inventoryResult = synchronizeInventory(request.inventory(), pharmacy);
        SyncEntityResult salesResult = synchronizeSales(request.sales(), pharmacy);

        log.info(
                "Synchronized pharmacy {}: inventory inserted={}, updated={}, ignored={}; sales inserted={}, updated={}, ignored={}",
                pharmacy.getId(),
                inventoryResult.inserted(),
                inventoryResult.updated(),
                inventoryResult.ignored(),
                salesResult.inserted(),
                salesResult.updated(),
                salesResult.ignored()
        );

        return new SyncResponse(pharmacy.getId(), inventoryResult, salesResult);
    }

    private SyncEntityResult synchronizeInventory(List<InventorySyncItem> incomingItems, Pharmacy pharmacy) {
        Map<UUID, CentralInventory> existingById = inventoryRepository.findAllById(idsOf(incomingItems, InventorySyncItem::id))
                .stream()
                .collect(Collectors.toMap(CentralInventory::getId, Function.identity()));

        List<CentralInventory> inserts = new ArrayList<>();
        int updated = 0;
        int ignored = 0;

        for (InventorySyncItem incoming : incomingItems) {
            CentralInventory existing = existingById.get(incoming.id());
            if (existing == null) {
                inserts.add(inventoryMapper.toEntity(incoming, pharmacy));
                continue;
            }

            verifyOwnership("Inventory", existing.getId(), existing.getPharmacy().getId(), pharmacy.getId());
            if (incoming.lastUpdatedAt().isAfter(existing.getLastUpdatedAt())) {
                inventoryMapper.updateEntity(existing, incoming);
                updated++;
            } else {
                ignored++;
            }
        }

        inventoryRepository.saveAll(inserts);
        return new SyncEntityResult(inserts.size(), updated, ignored);
    }

    private SyncEntityResult synchronizeSales(List<SaleSyncItem> incomingItems, Pharmacy pharmacy) {
        Map<UUID, CentralSale> existingById = saleRepository.findAllById(idsOf(incomingItems, SaleSyncItem::id))
                .stream()
                .collect(Collectors.toMap(CentralSale::getId, Function.identity()));

        List<CentralSale> inserts = new ArrayList<>();
        int updated = 0;
        int ignored = 0;

        for (SaleSyncItem incoming : incomingItems) {
            CentralSale existing = existingById.get(incoming.id());
            if (existing == null) {
                inserts.add(saleMapper.toEntity(incoming, pharmacy));
                continue;
            }

            verifyOwnership("Sales", existing.getId(), existing.getPharmacy().getId(), pharmacy.getId());
            if (incoming.lastUpdatedAt().isAfter(existing.getLastUpdatedAt())) {
                saleMapper.updateEntity(existing, incoming);
                updated++;
            } else {
                ignored++;
            }
        }

        saleRepository.saveAll(inserts);
        return new SyncEntityResult(inserts.size(), updated, ignored);
    }

    private <T> void assertUniqueIds(List<T> items, Function<T, UUID> idExtractor, String resourceType) {
        Set<UUID> seen = new HashSet<>();
        for (T item : items) {
            UUID id = idExtractor.apply(item);
            if (!seen.add(id)) {
                throw new DuplicateSyncRecordException(resourceType, id);
            }
        }
    }

    private <T> Set<UUID> idsOf(List<T> items, Function<T, UUID> idExtractor) {
        return items.stream()
                .map(idExtractor)
                .collect(Collectors.toSet());
    }

    private void verifyOwnership(String resourceType, UUID resourceId, UUID ownerPharmacyId, UUID expectedPharmacyId) {
        if (!ownerPharmacyId.equals(expectedPharmacyId)) {
            throw new SyncOwnershipConflictException(resourceType, resourceId);
        }
    }
}