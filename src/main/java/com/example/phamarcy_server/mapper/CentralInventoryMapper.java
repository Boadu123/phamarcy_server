package com.example.phamarcy_server.mapper;

import com.example.phamarcy_server.dto.InventoryResponse;
import com.example.phamarcy_server.dto.InventorySyncItem;
import com.example.phamarcy_server.entity.CentralInventory;
import com.example.phamarcy_server.entity.Pharmacy;
import org.springframework.stereotype.Component;

@Component
public class CentralInventoryMapper {

    public CentralInventory toEntity(InventorySyncItem item, Pharmacy pharmacy) {
        return new CentralInventory(
                item.id(),
                pharmacy,
                item.productName(),
                item.quantity(),
                item.price(),
                item.lastUpdatedAt()
        );
    }

    public void updateEntity(CentralInventory inventory, InventorySyncItem item) {
        inventory.setProductName(item.productName());
        inventory.setQuantity(item.quantity());
        inventory.setPrice(item.price());
        inventory.setLastUpdatedAt(item.lastUpdatedAt());
    }

    public InventoryResponse toResponse(CentralInventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getPharmacy().getId(),
                inventory.getProductName(),
                inventory.getQuantity(),
                inventory.getPrice(),
                inventory.getLastUpdatedAt()
        );
    }
}