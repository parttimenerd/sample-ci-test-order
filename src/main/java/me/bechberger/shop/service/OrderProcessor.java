package me.bechberger.shop.service;

import me.bechberger.shop.model.*;
import me.bechberger.shop.validation.OrderValidator;

/**
 * Orchestrates the full order processing pipeline:
 * validate → price → reserve inventory → apply shipping → confirm.
 */
public class OrderProcessor {

    private final PricingEngine pricingEngine;
    private final InventoryManager inventoryManager;
    private final ShippingCalculator shippingCalculator;
    private final OrderValidator orderValidator;
    private final NotificationService notificationService;

    public OrderProcessor(PricingEngine pricingEngine,
                          InventoryManager inventoryManager,
                          ShippingCalculator shippingCalculator,
                          OrderValidator orderValidator,
                          NotificationService notificationService) {
        this.pricingEngine = pricingEngine;
        this.inventoryManager = inventoryManager;
        this.shippingCalculator = shippingCalculator;
        this.orderValidator = orderValidator;
        this.notificationService = notificationService;
    }

    /**
     * Process an order end-to-end. Throws on validation or inventory failure.
     */
    public ProcessingResult processOrder(Order order) {
        // Step 1: Validate
        OrderValidator.ValidationResult validation = orderValidator.validate(order);
        if (!validation.isValid()) {
            return ProcessingResult.failed("Validation failed: " + validation.getErrors());
        }
        order.setStatus(Order.Status.VALIDATED);

        // Step 2: Price
        Money total = pricingEngine.priceOrder(order);

        // Step 3: Calculate shipping
        Address destination = order.getCustomer().getShippingAddress();
        Money shipping = shippingCalculator.calculateShipping(order, destination);
        order.setShippingCost(shipping);
        order.setTotal(total.add(shipping));

        // Step 4: Reserve inventory
        for (OrderItem item : order.getItems()) {
            boolean reserved = inventoryManager.reserve(
                    item.getProduct().getId(), item.getQuantity());
            if (!reserved) {
                // Roll back previous reservations
                rollbackReservations(order, item);
                return ProcessingResult.failed(
                        "Insufficient stock for: " + item.getProduct().getName());
            }
        }

        // Step 5: Confirm
        order.setStatus(Order.Status.CONFIRMED);
        order.getCustomer().incrementOrderCount();
        String confirmation = notificationService.buildConfirmation(order);

        return ProcessingResult.success(order, confirmation);
    }

    private void rollbackReservations(Order order, OrderItem failedItem) {
        for (OrderItem item : order.getItems()) {
            if (item == failedItem) break;
            inventoryManager.release(item.getProduct().getId(), item.getQuantity());
        }
    }

    /**
     * Result of order processing.
     */
    public static class ProcessingResult {
        private final boolean success;
        private final Order order;
        private final String message;

        private ProcessingResult(boolean success, Order order, String message) {
            this.success = success;
            this.order = order;
            this.message = message;
        }

        static ProcessingResult success(Order order, String confirmation) {
            return new ProcessingResult(true, order, confirmation);
        }

        static ProcessingResult failed(String reason) {
            return new ProcessingResult(false, null, reason);
        }

        public boolean isSuccess() { return success; }
        public Order getOrder() { return order; }
        public String getMessage() { return message; }
    }
}
