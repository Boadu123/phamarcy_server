package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.dto.SyncActivityStatus;
import com.example.phamarcy_server.entity.SyncActivity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncActivityRepository extends JpaRepository<SyncActivity, UUID> {

    long countByStatus(SyncActivityStatus status);
    long countByPharmacyIdAndStatus(UUID pharmacyId, SyncActivityStatus status);
    long countDistinctPharmacyIdByStatus(SyncActivityStatus status);
    Optional<SyncActivity> findTopByOrderByStartedAtDesc();
    Optional<SyncActivity> findTopByPharmacyIdOrderByStartedAtDesc(UUID pharmacyId);
    List<SyncActivity> findAllByOrderByStartedAtDesc(Pageable pageable);
    List<SyncActivity> findByPharmacyIdOrderByStartedAtDesc(UUID pharmacyId, Pageable pageable);
}
