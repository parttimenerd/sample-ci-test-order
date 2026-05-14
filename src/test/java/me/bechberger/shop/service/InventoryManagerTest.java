package me.bechberger.shop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryManagerTest {

    private InventoryManager manager;

    @BeforeEach
    void setUp() {
        manager = new InventoryManager();
        manager.setStock("P1", 100);
        manager.setStock("P2", 3);
    }

    @Test
    void availableEqualsStockMinusReserved() {
        manager.reserve("P1", 10);
        assertEquals(90, manager.getAvailable("P1"));
    }

    @Test
    void reserveSucceeds() {
        assertTrue(manager.reserve("P1", 50));
        assertEquals(50, manager.getAvailable("P1"));
    }

    @Test
    void reserveFailsWhenInsufficient() {
        assertFalse(manager.reserve("P1", 101));
        assertEquals(100, manager.getAvailable("P1")); // unchanged
    }

    @Test
    void releaseRestoresAvailability() {
        manager.reserve("P1", 20);
        manager.release("P1", 20);
        assertEquals(100, manager.getAvailable("P1"));
    }

    @Test
    void confirmDeductsStock() {
        manager.reserve("P1", 10);
        manager.confirmReservation("P1", 10);
        assertEquals(90, manager.getStockLevel("P1"));
        assertEquals(90, manager.getAvailable("P1")); // reserved also decremented
    }

    @Test
    void lowStockDetection() {
        assertTrue(manager.isLowStock("P2")); // 3 < 5
        assertFalse(manager.isLowStock("P1")); // 100 >= 5
    }

    @Test
    void getLowStockProducts() {
        var lowStock = manager.getLowStockProducts();
        assertTrue(lowStock.contains("P2"));
        assertFalse(lowStock.contains("P1"));
    }
}
