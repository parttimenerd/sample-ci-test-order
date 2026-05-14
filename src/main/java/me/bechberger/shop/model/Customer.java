package me.bechberger.shop.model;

import java.util.Objects;

/**
 * A customer who can place orders.
 */
public class Customer {

    private final String id;
    private String name;
    private String email;
    private Address shippingAddress;
    private int totalOrders;

    public Customer(String id, String name, String email, Address shippingAddress) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
        this.shippingAddress = shippingAddress;
        this.totalOrders = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = Objects.requireNonNull(email); }
    public Address getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Address addr) { this.shippingAddress = addr; }
    public int getTotalOrders() { return totalOrders; }
    public void incrementOrderCount() { this.totalOrders++; }

    /**
     * A customer is "loyal" if they have placed 5+ orders (eligible for discounts).
     */
    public boolean isLoyal() {
        return totalOrders >= 5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer c)) return false;
        return id.equals(c.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Customer{" + id + ", " + name + ", " + email + "}";
    }
}
