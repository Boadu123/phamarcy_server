package com.example.phamarcy_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pharmacies")
public class Pharmacy {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String location;

    @Column(name = "api_token", nullable = false, unique = true)
    private String apiToken;

    @OneToMany(mappedBy = "pharmacy")
    private List<CentralInventory> inventoryRecords = new ArrayList<>();

    @OneToMany(mappedBy = "pharmacy")
    private List<CentralSale> salesRecords = new ArrayList<>();

    protected Pharmacy() {
    }

    public Pharmacy(UUID id, String name, String location, String apiToken) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.apiToken = apiToken;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getApiToken() {
        return apiToken;
    }
}