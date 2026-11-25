package com.kalasetu.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class demonstrating Inheritance and Polymorphism across marketplace actors.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "role", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Customer.class, name = "CUSTOMER"),
    @JsonSubTypes.Type(value = Artisan.class, name = "ARTISAN"),
    @JsonSubTypes.Type(value = Admin.class, name = "ADMIN")
})
public abstract class User {
    private UUID id;
    private String fullName;
    private String email;
    @JsonIgnore
    private String passwordHash;
    private String phone;
    private String city;
    private String state;
    private boolean dpdpConsent;
    private boolean active = true;
    private OffsetDateTime createdAt;

    protected User() {}

    protected User(UUID id, String fullName, String email, String passwordHash,
                   String phone, String city, String state, boolean dpdpConsent) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.city = city;
        this.state = state;
        this.dpdpConsent = dpdpConsent;
        this.createdAt = OffsetDateTime.now();
    }

    public abstract Role getRole();

    /** Polymorphic capability list per user actor. */
    public abstract List<String> getDashboardCapabilities();

    /** Polymorphic commission / fee policy per actor type. */
    public abstract double calculatePlatformFeeRate();

    public boolean canListCraft() {
        return false;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public boolean isDpdpConsent() { return dpdpConsent; }
    public void setDpdpConsent(boolean dpdpConsent) { this.dpdpConsent = dpdpConsent; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
