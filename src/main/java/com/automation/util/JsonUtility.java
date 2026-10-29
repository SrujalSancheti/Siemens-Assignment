package com.automation.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class JsonUtility {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> LOCATOR_FILES = List.of("login", "inventory", "cart", "checkout");

    private JsonUtility() {
    }

    public static Locator locator(Page page, String key) {
        return page.locator(selector(key));
    }

    public static Locator locator(Page page, String key, String value) {
        return page.locator(selector(key).replace("{value}", xpathLiteral(value)));
    }

    public static String selector(String key) {
        for (String fileName : LOCATOR_FILES) {
            JsonNode locators = readFile(fileName);
            JsonNode selector = locators.get(key);
            if (selector != null && selector.isTextual()) {
                return selector.asText();
            }
        }
        throw new IllegalArgumentException("No locator named '" + key + "' exists in src/main/resources/locators.");
    }

    private static JsonNode readFile(String fileName) {
        String resource = "/locators/" + fileName + ".json";
        try (InputStream stream = JsonUtility.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Locator resource was not found: " + resource);
            }
            return MAPPER.readTree(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read locator resource: " + resource, exception);
        }
    }

    private static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        String[] parts = value.split("'", -1);
        StringBuilder expression = new StringBuilder("concat(");
        for (int index = 0; index < parts.length; index++) {
            if (index > 0) {
                expression.append(", \"'\", ");
            }
            expression.append("'").append(parts[index]).append("'");
        }
        return expression.append(")").toString();
    }
}
