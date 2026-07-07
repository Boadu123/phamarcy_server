package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.Batch;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRepository extends JpaRepository<Batch, UUID> {
}