package com.perfecthousehold;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility for loading key-value pairs from .env file into Java System properties
 * when running locally outside of Docker (e.g. directly in IntelliJ or Maven).
 */
public final class DotenvLoader {

    private DotenvLoader() {
    }

    public static void load() {
        Path envPath = Path.of(".env");
        if (!Files.exists(envPath)) {
            // Also check parent directory if run from sub-directory
            envPath = Path.of("../.env");
            if (!Files.exists(envPath)) {
                return;
            }
        }

        try (var lines = Files.lines(envPath)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#") && line.contains("="))
                    .forEach(line -> {
                        int idx = line.indexOf('=');
                        String key = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();

                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        // Set property only if not already supplied by OS environment
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    });
        } catch (IOException ignored) {
            // Fail silently if .env cannot be read
        }
    }
}
