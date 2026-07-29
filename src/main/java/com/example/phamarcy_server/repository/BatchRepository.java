package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.Batch;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BatchRepository extends JpaRepository<Batch, UUID> {

    Optional<Batch> findByPharmacy_IdAndStockReference(UUID pharmacyId, String stockReference);

    @Query("select b from Batch b join fetch b.product p where b.pharmacy.id = :pharmacyId and b.deleted = false order by p.name, b.stockReference")
    List<Batch> findActiveInventory(@Param("pharmacyId") UUID pharmacyId);

    long countByDeletedFalse();

    long countByPharmacy_IdAndDeletedFalse(UUID pharmacyId);

    @Query("select coalesce(sum(b.quantity), 0) from Batch b where b.deleted = false")
    Long calculateTotalUnitsInStock();

    @Query("select coalesce(sum(b.quantity), 0) from Batch b where b.pharmacy.id = :pharmacyId and b.deleted = false")
    Long calculateTotalUnitsInStockByPharmacyId(@Param("pharmacyId") UUID pharmacyId);

    @Query("select coalesce(sum(b.quantity * b.sellingPrice), 0) from Batch b where b.deleted = false")
    BigDecimal calculateTotalInventoryValue();

    @Query("select coalesce(sum(b.quantity * b.sellingPrice), 0) from Batch b where b.pharmacy.id = :pharmacyId and b.deleted = false")
    BigDecimal calculateTotalInventoryValueByPharmacyId(@Param("pharmacyId") UUID pharmacyId);
}
