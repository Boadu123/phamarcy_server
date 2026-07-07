package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.Pharmacy;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<Pharmacy, UUID> {

    Optional<Pharmacy> findByApiToken(String apiToken);
}