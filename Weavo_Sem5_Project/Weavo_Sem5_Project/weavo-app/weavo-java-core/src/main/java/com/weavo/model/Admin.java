package com.weavo.model;

/**
 * Admin / Evaluator class for platform governance and audit verification.
 * Demonstrates Inheritance from abstract User class.
 */
public class Admin extends User {
    private String department;

    public Admin(String id, String fullName, String email, String city, String state, 
                 boolean dpdpConsentAccepted, String department) {
        super(id, fullName, email, city, state, dpdpConsentAccepted);
        this.department = department;
    }

    @Override
    public String getRoleName() {
        return "ADMIN";
    }

    @Override
    public boolean canManageInventory() {
        return true;
    }

    @Override
    public boolean canPlaceOrders() {
        return true;
    }

    public String getDepartment() { return department; }
}
