package com.example.phamarcy_server.repository;

import com.example.phamarcy_server.entity.AppSetting;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, UUID> {
}