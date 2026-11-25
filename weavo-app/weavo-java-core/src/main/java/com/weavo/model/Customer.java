package com.weavo.model;

/**
 * Customer class representing buyers on the Weavo marketplace.
 * Demonstrates Inheritance from abstract User class.
 */
public class Customer extends User {
    private String shippingAddress;
    private int loyaltyPoints;

    public Customer(String id, String fullName, String email, String city, String state, 
                    boolean dpdpConsentAccepted, String shippingAddress) {
        super(id, fullName, email, city, state, dpdpConsentAccepted);
        this.shippingAddress = shippingAddress;
        this.loyaltyPoints = 0;
    }

    @Override
    public String getRoleName() {
        return "CUSTOMER";
    }

    @Override
    public boolean canManageInventory() {
        return false;
    }

    @Override
    public boolean canPlaceOrders() {
        return true;
    }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void addLoyaltyPoints(int points) { this.loyaltyPoints += points; }
}
