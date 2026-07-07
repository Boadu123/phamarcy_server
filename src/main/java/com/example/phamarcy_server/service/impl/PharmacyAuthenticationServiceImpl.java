package com.example.phamarcy_server.service.impl;

import com.example.phamarcy_server.exception.InvalidApiTokenException;
import com.example.phamarcy_server.repository.PharmacyRepository;
import com.example.phamarcy_server.security.AuthenticatedPharmacy;
import com.example.phamarcy_server.service.PharmacyAuthenticationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PharmacyAuthenticationServiceImpl implements PharmacyAuthenticationService {

    private final PharmacyRepository pharmacyRepository;

    public PharmacyAuthenticationServiceImpl(PharmacyRepository pharmacyRepository) {
        this.pharmacyRepository = pharmacyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedPharmacy authenticate(String apiToken) {
        if (!StringUtils.hasText(apiToken)) {
            throw new InvalidApiTokenException();
        }

        return pharmacyRepository.findByApiToken(apiToken.trim())
                .map(AuthenticatedPharmacy::from)
                .orElseThrow(InvalidApiTokenException::new);
    }
}