package com.example.phamarcy_server.security;

import com.example.phamarcy_server.service.PharmacyAuthenticationService;
import com.example.phamarcy_server.util.ApiPaths;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Pharmacy-Token";

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);

    private final PharmacyAuthenticationService pharmacyAuthenticationService;
    private final HandlerExceptionResolver exceptionResolver;

    public ApiKeyAuthenticationFilter(
            PharmacyAuthenticationService pharmacyAuthenticationService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.pharmacyAuthenticationService = pharmacyAuthenticationService;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && ApiPaths.SYNC.equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            AuthenticatedPharmacy authenticatedPharmacy = pharmacyAuthenticationService.authenticate(request.getHeader(HEADER_NAME));
            request.setAttribute(AuthenticatedPharmacy.REQUEST_ATTRIBUTE, authenticatedPharmacy);
            log.debug("Authenticated pharmacy {} for synchronization", authenticatedPharmacy.id());
            filterChain.doFilter(request, response);
        } catch (RuntimeException ex) {
            exceptionResolver.resolveException(request, response, null, ex);
        }
    }
}