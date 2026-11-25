package com.weavo.model;

/**
 * Abstract base class demonstrating OOP principles: Encapsulation, Abstraction, and Inheritance.
 * All marketplace actors (Buyer, Artisan, Admin) inherit from this class.
 */
public abstract class User {
    private String id;
    private String fullName;
    private String email;
    private String city;
    private String state;
    private boolean dpdpConsentAccepted;

    public User(String id, String fullName, String email, String city, String state, boolean dpdpConsentAccepted) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.city = city;
        this.state = state;
        this.dpdpConsentAccepted = dpdpConsentAccepted;
    }

    // Abstract methods enforced on all inheriting subclasses (Polymorphism)
    public abstract String getRoleName();
    public abstract boolean canManageInventory();
    public abstract boolean canPlaceOrders();

    // Encapsulated getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public boolean isDpdpConsentAccepted() { return dpdpConsentAccepted; }
    public void setDpdpConsentAccepted(boolean dpdpConsentAccepted) { this.dpdpConsentAccepted = dpdpConsentAccepted; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s, %s)", getRoleName(), fullName, city, state);
    }
}
