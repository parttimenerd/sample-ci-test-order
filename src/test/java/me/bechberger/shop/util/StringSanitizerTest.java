package me.bechberger.shop.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StringSanitizerTest {

    private StringSanitizer sanitizer = new StringSanitizer();

    @Test
    void stripSimpleHtml() {
        assertEquals("Hello", sanitizer.stripHtml("<b>Hello</b>"));
    }

    @Test
    void stripNestedHtml() {
        assertEquals("alert('xss')", sanitizer.stripHtml("<script><b>alert('xss')</b></script>"));
    }

    @Test
    void escapeSpecialChars() {
        String result = sanitizer.escapeSpecialChars("<script>alert('xss')</script>");
        assertFalse(result.contains("<"));
        assertTrue(result.contains("&lt;"));
    }

    @Test
    void normalizeWhitespace() {
        assertEquals("hello world", sanitizer.normalizeWhitespace("  hello   world  "));
    }

    @Test
    void sanitizeFullPipeline() {
        String result = sanitizer.sanitize("  <b>Hello</b>   <i>World</i>  ");
        assertEquals("Hello World", result);
    }
}
