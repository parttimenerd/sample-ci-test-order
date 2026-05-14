package me.bechberger.shop.validation;

import me.bechberger.shop.model.Address;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressValidatorTest {

    private AddressValidator validator = new AddressValidator();

    @Test
    void validUSAddress() {
        Address addr = new Address("123 Main St", "Springfield", "IL", "62701", "US");
        assertNull(validator.validate(addr));
    }

    @Test
    void validCanadianAddress() {
        Address addr = new Address("456 Queen St", "Toronto", "ON", "M5V 2A8", "CA");
        assertNull(validator.validate(addr));
    }

    @Test
    void missingStreetInvalid() {
        Address addr = new Address("", "City", "ST", "12345", "US");
        assertNotNull(validator.validate(addr));
    }

    @Test
    void unsupportedCountry() {
        Address addr = new Address("123 Main", "City", "ST", "12345", "ZZ");
        String error = validator.validate(addr);
        assertNotNull(error);
        assertTrue(error.contains("Unsupported"));
    }

    @Test
    void invalidUSZip() {
        Address addr = new Address("123 Main", "City", "ST", "ABCDE", "US");
        assertNotNull(validator.validate(addr));
    }

    @Test
    void supportedCountryCheck() {
        assertTrue(validator.isSupportedCountry("US"));
        assertTrue(validator.isSupportedCountry("GB"));
        assertFalse(validator.isSupportedCountry("ZZ"));
    }
}
