package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.CentralSale;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CentralSaleRepository extends JpaRepository<CentralSale, UUID> {

    List<CentralSale> findByPharmacyIdOrderByCreatedAtDesc(UUID pharmacyId);

    long countByPharmacyId(UUID pharmacyId);

    @Query("select coalesce(sum(s.totalAmount), 0) from CentralSale s")
    BigDecimal calculateTotalSalesAmount();

    @Query("select coalesce(sum(s.totalAmount), 0) from CentralSale s where s.pharmacy.id = :pharmacyId")
    BigDecimal calculateTotalSalesAmountByPharmacyId(@Param("pharmacyId") UUID pharmacyId);
}