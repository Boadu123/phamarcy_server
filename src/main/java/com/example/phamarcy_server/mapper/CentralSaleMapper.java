package com.example.phamarcy_server.mapper;

import com.example.phamarcy_server.dto.SaleResponse;
import com.example.phamarcy_server.dto.SaleSyncItem;
import com.example.phamarcy_server.entity.CentralSale;
import com.example.phamarcy_server.entity.Pharmacy;
import org.springframework.stereotype.Component;

@Component
public class CentralSaleMapper {

    public CentralSale toEntity(SaleSyncItem item, Pharmacy pharmacy) {
        return new CentralSale(
                item.id(),
                pharmacy,
                item.totalAmount(),
                item.createdAt(),
                item.lastUpdatedAt()
        );
    }

    public void updateEntity(CentralSale sale, SaleSyncItem item) {
        sale.setTotalAmount(item.totalAmount());
        sale.setCreatedAt(item.createdAt());
        sale.setLastUpdatedAt(item.lastUpdatedAt());
    }

    public SaleResponse toResponse(CentralSale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getPharmacy().getId(),
                sale.getTotalAmount(),
                sale.getCreatedAt(),
                sale.getLastUpdatedAt()
        );
    }
}