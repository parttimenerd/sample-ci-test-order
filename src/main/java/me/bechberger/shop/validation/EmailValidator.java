package me.bechberger.shop.validation;

import java.util.regex.Pattern;

/**
 * Validates email addresses (RFC-lite).
 */
public class EmailValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final int MAX_LENGTH = 254;

    /**
     * Check if the email is syntactically valid.
     */
    public boolean isValid(String email) {
        if (email == null || email.isBlank()) return false;
        if (email.length() > MAX_LENGTH) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Extract the domain part of an email.
     */
    public String extractDomain(String email) {
        if (email == null || !email.contains("@")) return null;
        return email.substring(email.lastIndexOf('@') + 1).toLowerCase();
    }

    /**
     * Check if the email uses a common free provider.
     */
    public boolean isFreeProvider(String email) {
        String domain = extractDomain(email);
        if (domain == null) return false;
        return domain.equals("gmail.com") || domain.equals("yahoo.com") ||
               domain.equals("hotmail.com") || domain.equals("outlook.com");
    }
}
