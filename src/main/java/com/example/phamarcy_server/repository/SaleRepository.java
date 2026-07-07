package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.Sale;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
}