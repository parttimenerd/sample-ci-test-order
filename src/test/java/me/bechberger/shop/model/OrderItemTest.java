package me.bechberger.shop.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private Product sampleProduct() {
        return new Product("P1", "Widget", Money.usd(9.99), "Electronics", 0.5);
    }

    @Test
    void lineTotal() {
        OrderItem item = new OrderItem(sampleProduct(), 3);
        assertEquals(Money.usd(29.97), item.getLineTotal());
    }

    @Test
    void totalWeight() {
        OrderItem item = new OrderItem(sampleProduct(), 4);
        assertEquals(2.0, item.getTotalWeight(), 0.001);
    }

    @Test
    void quantityMustBePositive() {
        assertThrows(IllegalArgumentException.class, () ->
                new OrderItem(sampleProduct(), 0));
    }

    @Test
    void setQuantity() {
        OrderItem item = new OrderItem(sampleProduct(), 1);
        item.setQuantity(5);
        assertEquals(5, item.getQuantity());
    }

    @Test
    void snapshotsPrice() {
        Product p = sampleProduct();
        OrderItem item = new OrderItem(p, 1);
        p.setPrice(Money.usd(99.99)); // change price after order
        assertEquals(Money.usd(9.99), item.getUnitPrice()); // snapshot unchanged
    }
}
