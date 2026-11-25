package com.kalasetu.web.controller;

import com.kalasetu.domain.exception.InvalidInputException;
import com.kalasetu.domain.exception.UnauthorizedActionException;
import com.kalasetu.domain.model.*;
import com.kalasetu.domain.security.InputSanitizer;
import com.kalasetu.domain.security.PasswordHasher;
import com.kalasetu.fileio.CatalogFileService;
import com.kalasetu.persistence.MarketplaceRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final MarketplaceRepository repository;
    private final CatalogFileService fileService;

    public AuthController(MarketplaceRepository repository, CatalogFileService fileService) {
        this.repository = repository;
        this.fileService = fileService;
    }

    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(
            String fullName, String email, String password, String role,
            String phone, String city, String state, boolean dpdpConsent,
            String workshopName, String villageCluster, String craftSpecialty, String giTagNumber
    ) {}

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req)
            throws InvalidInputException, UnauthorizedActionException {
        String email = InputSanitizer.validateEmail(req.email());
        if (req.password() == null || req.password().isBlank()) {
            throw new InvalidInputException("password", "Password is required");
        }
        User user = repository.findUserByEmail(email)
                .orElseThrow(() -> new UnauthorizedActionException("Invalid email or password"));
        if (!PasswordHasher.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedActionException("Invalid email or password");
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("user", user);
        resp.put("capabilities", user.getDashboardCapabilities());
        resp.put("platformFeeRate", user.calculatePlatformFeeRate());
        if (user instanceof Artisan) {
            repository.findArtisanByUserId(user.getId())
                    .ifPresent(ap -> resp.put("artisanProfile", ap));
        }
        return resp;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest req) throws Exception {
        if (!req.dpdpConsent()) {
            throw new InvalidInputException("dpdpConsent", "Consent under India DPDP Act 2023 is required to create an account");
        }
        String fullName = InputSanitizer.sanitizeText("fullName", req.fullName(), 120);
        String email = InputSanitizer.validateEmail(req.email());
        if (req.password() == null || req.password().length() < 6) {
            throw new InvalidInputException("password", "Password must be at least 6 characters");
        }
        if (repository.findUserByEmail(email).isPresent()) {
            throw new InvalidInputException("email", "An account with this email already exists");
        }
        String roleStr = req.role() == null ? "CUSTOMER" : req.role().trim().toUpperCase();
        if ("ADMIN".equals(roleStr)) {
            throw new UnauthorizedActionException("Self-registration as ADMIN is prohibited");
        }

        String hash = PasswordHasher.hash(req.password());
        String city = req.city() != null && !req.city().isBlank()
                ? InputSanitizer.sanitizeText("city", req.city(), 60) : "Ahmedabad";
        String state = req.state() != null && !req.state().isBlank()
                ? InputSanitizer.sanitizeText("state", req.state(), 60) : "Gujarat";
        String phone = req.phone() != null ? req.phone().trim() : "+91-9000000000";

        User created;
        ArtisanProfile artisanProfile = null;
        if ("ARTISAN".equals(roleStr)) {
            String workshop = InputSanitizer.sanitizeText("workshopName",
                    req.workshopName() != null ? req.workshopName() : fullName + " Craft Studio", 120);
            String cluster = InputSanitizer.sanitizeText("villageCluster",
                    req.villageCluster() != null ? req.villageCluster() : city, 120);

            Artisan artisan = new Artisan(null, fullName, email, hash, phone, city, state, true,
                    workshop, cluster, req.giTagNumber(), "PENDING", "STARTER_FREE");
            created = repository.createUser(artisan);

            ArtisanProfile ap = new ArtisanProfile();
            ap.setUserId(created.getId());
            ap.setWorkshopName(workshop);
            ap.setVillageCluster(cluster);
            ap.setDistrict(city);
            ap.setState(state);
            ap.setCraftSpecialty(req.craftSpecialty() != null ? req.craftSpecialty() : "Regional Handicrafts");
            ap.setGiTagNumber(req.giTagNumber());
            ap.setKycStatus("PENDING");
            ap.setSubscriptionTier("STARTER_FREE");
            ap.setCommissionRate(new BigDecimal(artisan.calculatePlatformFeeRate() * 100));
            ap.setBio("Registered village craft workshop via Weavo onboarding");
            artisanProfile = repository.createArtisanProfile(ap);
        } else {
            Customer customer = new Customer(null, fullName, email, hash, phone, city, state, true);
            created = repository.createUser(customer);
        }

        AuditEvent ev = new AuditEvent();
        ev.setActorUserId(created.getId());
        ev.setEventType("USER_REGISTERED");
        ev.setEntityType("USER");
        ev.setEntityId(created.getId().toString());
        ev.setDetails(Map.of("role", created.getRole().name()));
        repository.saveAuditEvent(ev);
        fileService.appendAuditLog(ev);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("user", created);
        resp.put("capabilities", created.getDashboardCapabilities());
        if (artisanProfile != null) resp.put("artisanProfile", artisanProfile);
        return resp;
    }

    @GetMapping("/demo-accounts")
    public List<Map<String, String>> demoAccounts() {
        return List.of(
                Map.of("role", "CUSTOMER", "name", "Aarav Patel (Buyer - Ahmedabad)", "email", "aarav.buyer@weavo.demo", "hint", "Weavo@2026"),
                Map.of("role", "ARTISAN", "name", "Rameshbhai Khatri (Jamnagar Bandhani)", "email", "ramesh.artisan@weavo.demo", "hint", "Weavo@2026"),
                Map.of("role", "ARTISAN", "name", "Jiviben Ahir (Bhujodi Co-op)", "email", "jiviben.artisan@weavo.demo", "hint", "Weavo@2026"),
                Map.of("role", "ADMIN", "name", "Weavo Admin", "email", "admin@weavo.demo", "hint", "Weavo@2026")
        );
    }
}
