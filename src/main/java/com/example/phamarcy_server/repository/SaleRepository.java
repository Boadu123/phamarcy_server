package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.Sale;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    @Query("select distinct s from Sale s join fetch s.user left join fetch s.items where s.pharmacy.id = :pharmacyId and s.deleted = false order by s.saleDate desc")
    List<Sale> findActiveSales(@Param("pharmacyId") UUID pharmacyId);

    @Query("""
            select s
            from Sale s
            join fetch s.pharmacy p
            join fetch s.user
            where s.id = :saleId
              and p.id = :pharmacyId
              and s.deleted = false
            """)
    Optional<Sale> findActiveByIdAndPharmacyId(
            @Param("saleId") UUID saleId,
            @Param("pharmacyId") UUID pharmacyId
    );

    long countByDeletedFalse();

    long countByPharmacy_IdAndDeletedFalse(UUID pharmacyId);

    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s where s.deleted = false")
    BigDecimal calculateTotalSalesAmount();

    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s where s.pharmacy.id = :pharmacyId and s.deleted = false")
    BigDecimal calculateTotalSalesAmountByPharmacyId(@Param("pharmacyId") UUID pharmacyId);
}
