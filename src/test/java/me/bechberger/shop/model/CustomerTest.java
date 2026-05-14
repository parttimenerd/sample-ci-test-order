package me.bechberger.shop.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Address sampleAddress() {
        return new Address("123 Main St", "Springfield", "IL", "62701", "US");
    }

    @Test
    void createCustomer() {
        Customer c = new Customer("C1", "Alice", "alice@example.com", sampleAddress());
        assertEquals("C1", c.getId());
        assertEquals("Alice", c.getName());
        assertEquals("alice@example.com", c.getEmail());
    }

    @Test
    void newCustomerNotLoyal() {
        Customer c = new Customer("C1", "Alice", "alice@example.com", sampleAddress());
        assertFalse(c.isLoyal());
        assertEquals(0, c.getTotalOrders());
    }

    @Test
    void loyalAfterFiveOrders() {
        Customer c = new Customer("C1", "Alice", "alice@example.com", sampleAddress());
        for (int i = 0; i < 5; i++) c.incrementOrderCount();
        assertTrue(c.isLoyal());
    }

    @Test
    void equalityById() {
        Customer a = new Customer("C1", "Alice", "a@x.com", sampleAddress());
        Customer b = new Customer("C1", "Bob", "b@x.com", sampleAddress());
        assertEquals(a, b);
    }

    @Test
    void updateEmail() {
        Customer c = new Customer("C1", "Alice", "old@x.com", sampleAddress());
        c.setEmail("new@x.com");
        assertEquals("new@x.com", c.getEmail());
    }
}
