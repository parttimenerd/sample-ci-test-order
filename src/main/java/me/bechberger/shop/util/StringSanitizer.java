package me.bechberger.shop.util;

import java.util.regex.Pattern;

/**
 * Sanitizes user-provided strings for safe display.
 */
public class StringSanitizer {

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s{2,}");

    /**
     * Strip all HTML tags from the input.
     */
    public String stripHtml(String input) {
        if (input == null) return "";
        String result = input;
        // Iteratively strip nested tags
        String previous;
        do {
            previous = result;
            result = HTML_TAG.matcher(result).replaceAll("");
        } while (!result.equals(previous));
        // Clean up any leftover angle brackets
        result = result.replace("<", "").replace(">", "");
        return result;
    }

    /**
     * Escape special characters for safe inclusion in output.
     */
    public String escapeSpecialChars(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Normalize whitespace: trim and collapse multiple spaces.
     */
    public String normalizeWhitespace(String input) {
        if (input == null) return "";
        return MULTI_SPACE.matcher(input.trim()).replaceAll(" ");
    }

    /**
     * Full sanitization: strip HTML, normalize whitespace, trim.
     */
    public String sanitize(String input) {
        if (input == null) return "";
        String result = stripHtml(input);
        result = normalizeWhitespace(result);
        return result;
    }
}
