package me.bechberger.shop.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Customer sampleCustomer() {
        Address addr = new Address("123 Main St", "Springfield", "IL", "62701", "US");
        return new Customer("C1", "Alice", "alice@example.com", addr);
    }

    private Product sampleProduct(String id, double price) {
        return new Product(id, "Product " + id, Money.usd(price), "General", 1.0);
    }

    @Test
    void newOrderIsDraft() {
        Order order = new Order("O1", sampleCustomer());
        assertEquals(Order.Status.DRAFT, order.getStatus());
    }

    @Test
    void addItems() {
        Order order = new Order("O1", sampleCustomer());
        order.addItem(new OrderItem(sampleProduct("P1", 10.00), 2));
        order.addItem(new OrderItem(sampleProduct("P2", 5.00), 1));
        assertEquals(2, order.getItemCount());
        assertEquals(3, order.getTotalQuantity());
    }

    @Test
    void totalWeight() {
        Order order = new Order("O1", sampleCustomer());
        order.addItem(new OrderItem(sampleProduct("P1", 10.00), 3)); // 3 * 1.0kg
        assertEquals(3.0, order.getTotalWeight(), 0.001);
    }

    @Test
    void cannotAddItemToConfirmedOrder() {
        Order order = new Order("O1", sampleCustomer());
        order.setStatus(Order.Status.CONFIRMED);
        assertThrows(IllegalStateException.class, () ->
                order.addItem(new OrderItem(sampleProduct("P1", 10.00), 1)));
    }

    @Test
    void removeItem() {
        Order order = new Order("O1", sampleCustomer());
        Product p = sampleProduct("P1", 10.00);
        order.addItem(new OrderItem(p, 2));
        order.removeItem(p);
        assertEquals(0, order.getItemCount());
    }

    @Test
    void statusTransitions() {
        Order order = new Order("O1", sampleCustomer());
        order.setStatus(Order.Status.VALIDATED);
        assertEquals(Order.Status.VALIDATED, order.getStatus());
        order.setStatus(Order.Status.PRICED);
        assertEquals(Order.Status.PRICED, order.getStatus());
    }

    @Test
    void initialTotalsAreZero() {
        Order order = new Order("O1", sampleCustomer());
        assertTrue(order.getSubtotal().isZero());
        assertTrue(order.getTax().isZero());
        assertTrue(order.getTotal().isZero());
    }
}
