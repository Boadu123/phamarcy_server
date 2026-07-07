package com.example.phamarcy_server.service;

import com.example.phamarcy_server.security.AuthenticatedPharmacy;

public interface PharmacyAuthenticationService {

    AuthenticatedPharmacy authenticate(String apiToken);
}