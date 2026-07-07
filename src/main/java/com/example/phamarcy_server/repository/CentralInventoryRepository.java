package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.CentralInventory;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CentralInventoryRepository extends JpaRepository<CentralInventory, UUID> {

    List<CentralInventory> findByPharmacyIdOrderByProductNameAsc(UUID pharmacyId);

    long countByPharmacyId(UUID pharmacyId);

    @Query(value = "select coalesce(sum(quantity * price), 0) from central_inventory", nativeQuery = true)
    BigDecimal calculateTotalInventoryValue();

    @Query(value = "select coalesce(sum(quantity * price), 0) from central_inventory where pharmacy_id = :pharmacyId", nativeQuery = true)
    BigDecimal calculateTotalInventoryValueByPharmacyId(@Param("pharmacyId") UUID pharmacyId);
}