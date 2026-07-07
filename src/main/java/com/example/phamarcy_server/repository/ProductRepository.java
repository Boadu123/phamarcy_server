package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}