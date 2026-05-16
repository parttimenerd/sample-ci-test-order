package me.bechberger.shop.service;

import me.bechberger.shop.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PricingEngineTest {

    private PricingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PricingEngine();
    }

    private Order createOrder(double price, int quantity) {
        Address addr = new Address("123 Main", "City", "ST", "12345", "US");
        Customer customer = new Customer("C1", "Alice", "a@x.com", addr);
        Order order = new Order("O1", customer);
        Product product = new Product("P1", "Item", Money.usd(price), "General", 1.0);
        order.addItem(new OrderItem(product, quantity));
        return order;
    }

    @Test
    void subtotalSingleItem() {
        Order order = createOrder(25.00, 2);
        Money subtotal = engine.calculateSubtotal(order);
        assertEquals(Money.usd(50.00), subtotal);
    }

    @Test
    void subtotalMultipleItems() {
        Address addr = new Address("123 Main", "City", "ST", "12345", "US");
        Customer customer = new Customer("C1", "Alice", "a@x.com", addr);
        Order order = new Order("O1", customer);
        order.addItem(new OrderItem(new Product("P1", "A", Money.usd(10.00), "G", 1.0), 2));
        order.addItem(new OrderItem(new Product("P2", "B", Money.usd(5.00), "G", 1.0), 3));
        assertEquals(Money.usd(35.00), engine.calculateSubtotal(order));
    }

    @Test
    void noBulkDiscountUnderThreshold() {
        Order order = createOrder(10.00, 5); // 5 items < 10
        Money discount = engine.calculateBulkDiscount(order, Money.usd(50.00));
        assertTrue(discount.isZero());
    }

    @Test
    void bulkDiscountAtThreshold() {
        Order order = createOrder(10.00, 10); // exactly 10
        Money discount = engine.calculateBulkDiscount(order, Money.usd(100.00));
        assertEquals(Money.usd(5.00), discount); // 5% of 100
    }

    @Test
    void taxCalculation() {
        Money tax = engine.calculateTax(Money.usd(100.00));
        assertEquals(Money.usd(8.00), tax); // 8%
    }

    @Test
    void priceOrderSetsStatusToPriced() {
        Order order = createOrder(25.00, 2);
        engine.priceOrder(order);
        assertEquals(Order.Status.PRICED, order.getStatus());
    }

    @Test
    void priceOrderCalculatesTotal() {
        Order order = createOrder(25.00, 2); // subtotal = 50, no bulk, tax = 4
        Money total = engine.priceOrder(order);
        assertEquals(Money.usd(54.00), total); // 50 + 4
    }

    @Test
    void loyaltyDiscountForLoyalCustomer() {
        Address addr = new Address("123 Main", "City", "ST", "12345", "US");
        Customer customer = new Customer("C1", "Alice", "a@x.com", addr);
        for (int i = 0; i < 5; i++) customer.incrementOrderCount();
        assertTrue(customer.isLoyal());
        Money discount = engine.calculateLoyaltyDiscount(customer, Money.usd(100.00));
        assertEquals(Money.usd(10.00), discount); // 10% of 100
    }

    @Test
    void noLoyaltyDiscountForNewCustomer() {
        Address addr = new Address("123 Main", "City", "ST", "12345", "US");
        Customer customer = new Customer("C2", "Bob", "b@x.com", addr);
        assertFalse(customer.isLoyal());
        Money discount = engine.calculateLoyaltyDiscount(customer, Money.usd(100.00));
        assertTrue(discount.isZero());
    }
}
