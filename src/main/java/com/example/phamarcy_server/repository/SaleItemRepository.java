package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.SaleItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

    @Query("""
            select si
            from SaleItem si
            join fetch si.batch b
            join fetch b.product
            where si.sale.id = :saleId
              and si.pharmacy.id = :pharmacyId
              and si.deleted = false
            order by si.createdAt, si.id
            """)
    List<SaleItem> findActiveBySaleIdAndPharmacyId(
            @Param("saleId") UUID saleId,
            @Param("pharmacyId") UUID pharmacyId
    );
}
