package me.bechberger.shop.validation;

import java.util.ArrayList;
import java.util.List;

import me.bechberger.shop.model.Order;
import me.bechberger.shop.model.OrderItem;
import me.bechberger.shop.service.InventoryManager;

/**
 * Validates an order before processing — checks address, email, stock, limits.
 */
public class OrderValidator {

    private static final int MAX_ITEMS = 50;
    private static final int MAX_QUANTITY_PER_ITEM = 100;

    private final AddressValidator addressValidator;
    private final EmailValidator emailValidator;
    private final InventoryManager inventoryManager;

    public OrderValidator(AddressValidator addressValidator,
                          EmailValidator emailValidator,
                          InventoryManager inventoryManager) {
        this.addressValidator = addressValidator;
        this.emailValidator = emailValidator;
        this.inventoryManager = inventoryManager;
    }

    /**
     * Run all validations on the order.
     */
    public ValidationResult validate(Order order) {
        List<String> errors = new ArrayList<>();

        // Must have items
        if (order.getItems().isEmpty()) {
            errors.add("Order must contain at least one item");
        }

        // Item count limit
        if (order.getItems().size() > MAX_ITEMS) {
            errors.add("Order exceeds maximum of " + MAX_ITEMS + " items");
        }

        // Per-item quantity limit
        for (OrderItem item : order.getItems()) {
            if (item.getQuantity() > MAX_QUANTITY_PER_ITEM) {
                errors.add("Item '" + item.getProduct().getName() +
                           "' exceeds max quantity of " + MAX_QUANTITY_PER_ITEM);
            }
        }

        // Validate customer email
        if (!emailValidator.isValid(order.getCustomer().getEmail())) {
            errors.add("Invalid customer email: " + order.getCustomer().getEmail());
        }

        // Validate shipping address
        String addressError = addressValidator.validate(order.getCustomer().getShippingAddress());
        if (addressError != null) {
            errors.add("Address: " + addressError);
        }

        // Check stock availability
        for (OrderItem item : order.getItems()) {
            if (!inventoryManager.isAvailable(item.getProduct().getId(), item.getQuantity())) {
                errors.add("Insufficient stock for '" + item.getProduct().getName() +
                           "' (requested: " + item.getQuantity() +
                           ", available: " + inventoryManager.getAvailable(item.getProduct().getId()) + ")");
            }
        }

        return new ValidationResult(errors);
    }

    /**
     * Result of order validation.
     */
    public static class ValidationResult {
        private final List<String> errors;

        public ValidationResult(List<String> errors) {
            this.errors = List.copyOf(errors);
        }

        public boolean isValid() { return errors.isEmpty(); }
        public List<String> getErrors() { return errors; }

        @Override
        public String toString() {
            return isValid() ? "VALID" : "INVALID: " + errors;
        }
    }
}
