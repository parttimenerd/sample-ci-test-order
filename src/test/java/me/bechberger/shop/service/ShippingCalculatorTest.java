package me.bechberger.shop.service;

import me.bechberger.shop.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShippingCalculatorTest {

    private ShippingCalculator calc = new ShippingCalculator();

    private Order createOrder(double price, int quantity, double weightKg) {
        Address addr = new Address("123 Main", "City", "ST", "12345", "US");
        Customer customer = new Customer("C1", "Alice", "a@x.com", addr);
        Order order = new Order("O1", customer);
        Product product = new Product("P1", "Item", Money.usd(price), "General", weightKg);
        order.addItem(new OrderItem(product, quantity));
        return order;
    }

    @Test
    void domesticShipping() {
        Order order = createOrder(10.00, 1, 2.0);
        Address dest = new Address("456 Oak", "Town", "CA", "90210", "US");
        Money cost = calc.calculateStandard(order, dest);
        // 2.0kg * $0.50 + $3.99 = $4.99
        assertEquals(Money.usd(4.99), cost);
    }

    @Test
    void canadaShipping() {
        Order order = createOrder(10.00, 1, 2.0);
        Address dest = new Address("789 Queen", "Toronto", "ON", "M5V 2A8", "CA");
        Money cost = calc.calculateStandard(order, dest);
        // 2.0kg * $1.20 + $3.99 = $6.39
        assertEquals(Money.usd(6.39), cost);
    }

    @Test
    void internationalShipping() {
        Order order = createOrder(10.00, 1, 2.0);
        Address dest = new Address("221B Baker", "London", "ENG", "NW1", "GB");
        Money cost = calc.calculateStandard(order, dest);
        // 2.0kg * $2.50 + $3.99 = $8.99
        assertEquals(Money.usd(8.99), cost);
    }

    @Test
    void freeShippingOver50() {
        Order order = createOrder(60.00, 1, 2.0);
        // Need to set subtotal for free shipping check
        order.setSubtotal(Money.usd(60.00));
        assertTrue(calc.qualifiesForFreeShipping(order));
    }

    @Test
    void noFreeShippingUnder50() {
        Order order = createOrder(30.00, 1, 2.0);
        order.setSubtotal(Money.usd(30.00));
        assertFalse(calc.qualifiesForFreeShipping(order));
    }

    @Test
    void calculateShippingAppliesFreeThreshold() {
        Order order = createOrder(60.00, 1, 2.0);
        order.setSubtotal(Money.usd(60.00));
        Address dest = new Address("123 Main", "City", "ST", "12345", "US");
        Money cost = calc.calculateShipping(order, dest);
        assertTrue(cost.isZero());
    }
}
