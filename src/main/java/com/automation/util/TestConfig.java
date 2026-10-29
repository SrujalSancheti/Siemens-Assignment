package com.automation.util;

public final class TestConfig {
    private TestConfig() {
    }

    public static String browserName() {
        return System.getProperty("browser", "chromium").trim().toLowerCase();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", "false"));
    }

    public static String baseUrl() {
        return System.getProperty("baseUrl", "https://www.saucedemo.com/").replaceAll("/+$", "");
    }

    public static String apiBaseUrl() {
        return System.getProperty("apiBaseUrl", "https://restful-booker.herokuapp.com").replaceAll("/+$", "");
    }
}
