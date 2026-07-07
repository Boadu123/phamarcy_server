package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.SaleItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {
}