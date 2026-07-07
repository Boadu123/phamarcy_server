package com.example.phamarcy_server.security;

import com.example.phamarcy_server.entity.Pharmacy;
import java.util.UUID;

public record AuthenticatedPharmacy(
        UUID id,
        String name
) {
    public static final String REQUEST_ATTRIBUTE = "authenticatedPharmacy";

    public static AuthenticatedPharmacy from(Pharmacy pharmacy) {
        return new AuthenticatedPharmacy(pharmacy.getId(), pharmacy.getName());
    }
}