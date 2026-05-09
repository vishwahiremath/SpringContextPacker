package com.vishwahiremath.springContextPacker.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Redacts sensitive configuration values from application.properties/yml.
 */
public class ConfigSanitizer {
    
    // Redacts values for keys containing "password", "secret", "key", or "token"
    private static final Pattern SENSITIVE_KEYS = Pattern.compile("(?i).*(password|secret|key|token).*");
    
    public static Map<String, String> sanitize(Map<String, String> properties) {
        Map<String, String> sanitized = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (SENSITIVE_KEYS.matcher(entry.getKey()).matches()) {
                sanitized.put(entry.getKey(), "***REDACTED***");
            } else {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }
        return sanitized;
    }
}
