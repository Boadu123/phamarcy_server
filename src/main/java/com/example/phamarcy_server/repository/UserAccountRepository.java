package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.UserAccount;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
}