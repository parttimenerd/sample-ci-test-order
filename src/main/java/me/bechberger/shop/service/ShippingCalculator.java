package me.bechberger.shop.service;

import me.bechberger.shop.model.Address;
import me.bechberger.shop.model.Money;
import me.bechberger.shop.model.Order;

/**
 * Calculates shipping cost based on weight and destination zone.
 */
public class ShippingCalculator {

    // Base rates per zone (per kg)
    private static final double ZONE_1_RATE = 0.50;  // US domestic
    private static final double ZONE_2_RATE = 1.20;  // North America
    private static final double ZONE_3_RATE = 2.50;  // International

    // Flat handling fee
    private static final double HANDLING_FEE = 3.99;

    /**
     * Calculate standard shipping cost.
     */
    public Money calculateStandard(Order order, Address destination) {
        double weight = order.getTotalWeight();
        int zone = destination.getShippingZone();
        double rate = getRateForZone(zone);
        double cost = (weight * rate) + HANDLING_FEE;
        return Money.of(cost, "USD");
    }

    /**
     * Free shipping for orders with subtotal > $50.
     */
    public boolean qualifiesForFreeShipping(Order order) {
        return order.getSubtotal().isGreaterThan(Money.usd(50.0));
    }

    /**
     * Calculate shipping, applying free shipping if eligible.
     */
    public Money calculateShipping(Order order, Address destination) {
        if (qualifiesForFreeShipping(order)) {
            return Money.ZERO;
        }
        return calculateStandard(order, destination);
    }

    private double getRateForZone(int zone) {
        return switch (zone) {
            case 1 -> ZONE_1_RATE;
            case 2 -> ZONE_2_RATE;
            default -> ZONE_3_RATE;
        };
    }
}
