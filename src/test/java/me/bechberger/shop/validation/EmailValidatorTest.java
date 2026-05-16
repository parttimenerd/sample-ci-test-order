package me.bechberger.shop.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    private EmailValidator validator = new EmailValidator();

    @Test
    void validEmail() {
        assertTrue(validator.isValid("user@example.com"));
    }

    @Test
    void validEmailWithPlus() {
        assertTrue(validator.isValid("user+tag@example.com"));
    }

    @Test
    void invalidEmailNoAt() {
        assertFalse(validator.isValid("userexample.com"));
    }

    @Test
    void invalidEmailEmpty() {
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid(null));
    }

    @Test
    void extractDomain() {
        assertEquals("example.com", validator.extractDomain("user@example.com"));
    }

    @Test
    void isFreeProvider() {
        assertTrue(validator.isFreeProvider("user@gmail.com"));
        assertTrue(validator.isFreeProvider("user@yahoo.com"));
        assertFalse(validator.isFreeProvider("user@company.com"));
    }

    @Test
    void disposableProviderDetected() {
        assertTrue(validator.isDisposableProvider("x@tempmail.com"));
        assertTrue(validator.isDisposableProvider("x@mailinator.com"));
    }

    @Test
    void normalEmailNotDisposable() {
        assertFalse(validator.isDisposableProvider("user@company.com"));
        assertFalse(validator.isDisposableProvider("user@gmail.com"));
    }
}
