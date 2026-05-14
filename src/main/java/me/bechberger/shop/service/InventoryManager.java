package me.bechberger.shop.service;

import java.util.*;

/**
 * Manages product stock levels: check, reserve, and release.
 */
public class InventoryManager {

    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, Integer> reserved = new HashMap<>();

    public void setStock(String productId, int quantity) {
        stock.put(productId, quantity);
        reserved.putIfAbsent(productId, 0);
    }

    /**
     * Available = total stock - reserved.
     */
    public int getAvailable(String productId) {
        int total = stock.getOrDefault(productId, 0);
        int res = reserved.getOrDefault(productId, 0);
        return total - res;
    }

    public int getStockLevel(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    /**
     * Check if the requested quantity is available.
     */
    public boolean isAvailable(String productId, int quantity) {
        return getAvailable(productId) >= quantity;
    }

    /**
     * Reserve stock for an order. Returns true if successful.
     */
    public boolean reserve(String productId, int quantity) {
        if (!isAvailable(productId, quantity)) {
            return false;
        }
        reserved.merge(productId, quantity, Integer::sum);
        return true;
    }

    /**
     * Release previously reserved stock (e.g. order cancelled).
     */
    public void release(String productId, int quantity) {
        int currentReserved = reserved.getOrDefault(productId, 0);
        reserved.put(productId, Math.max(0, currentReserved - quantity));
    }

    /**
     * Confirm a reservation: deduct from actual stock.
     */
    public void confirmReservation(String productId, int quantity) {
        int currentReserved = reserved.getOrDefault(productId, 0);
        int currentStock = stock.getOrDefault(productId, 0);
        reserved.put(productId, Math.max(0, currentReserved - quantity));
        stock.put(productId, Math.max(0, currentStock - quantity));
    }

    /**
     * Check if stock is below the low-stock threshold (5 units).
     */
    public boolean isLowStock(String productId) {
        return getAvailable(productId) < 5;
    }

    /**
     * Get all products that are low on stock.
     */
    public List<String> getLowStockProducts() {
        List<String> result = new ArrayList<>();
        for (String productId : stock.keySet()) {
            if (isLowStock(productId)) {
                result.add(productId);
            }
        }
        return result;
    }
}
