package com.interview.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnvUtil {

    private static final String ENV_FILE_NAME = ".env";
    private static final Map<String, String> ENV_FILE_VALUES = loadEnvFile();

    private EnvUtil() {
    }

    public static String getOptional(String key, String defaultValue) {
        String value = System.getenv(key);

        if (value != null && !value.isBlank()) {
            return value;
        }

        String envFileValue = ENV_FILE_VALUES.get(key);

        if (envFileValue != null && !envFileValue.isBlank()) {
            return envFileValue;
        }

        return defaultValue;
    }

    public static String getRequired(String key) {
        String value = getOptional(key, null);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required configuration: " + key + ". Set it in environment variables or in .env"
            );
        }

        return value;
    }

    private static Map<String, String> loadEnvFile() {
        Path envPath = Path.of(ENV_FILE_NAME);

        if (!Files.exists(envPath)) {
            return Map.of();
        }

        try {
            List<String> lines = Files.readAllLines(envPath);
            Map<String, String> values = new HashMap<>();

            for (String rawLine : lines) {
                String line = rawLine.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int separatorIndex = line.indexOf('=');

                if (separatorIndex <= 0) {
                    continue;
                }

                String key = line.substring(0, separatorIndex).trim();
                String value = line.substring(separatorIndex + 1).trim();

                values.put(key, stripQuotes(value));
            }

            return Map.copyOf(values);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file", e);
        }
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            boolean hasDoubleQuotes = value.startsWith("\"") && value.endsWith("\"");
            boolean hasSingleQuotes = value.startsWith("'") && value.endsWith("'");

            if (hasDoubleQuotes || hasSingleQuotes) {
                return value.substring(1, value.length() - 1);
            }
        }

        return value;
    }
}
