package com.automation.util;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public final class BrowserManager {
    private static final ThreadLocal<BrowserSession> SESSION = new ThreadLocal<>();

    private BrowserManager() {
    }

    public static void start(String browserName) {
        close();
        Playwright playwright = Playwright.create();
        try {
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                    .setHeadless(TestConfig.headless());
            Browser browser = switch (browserName.trim().toLowerCase()) {
                case "chromium", "chrome" -> playwright.chromium().launch(options);
                case "firefox" -> playwright.firefox().launch(options);
                case "webkit", "safari" -> playwright.webkit().launch(options);
                default -> throw new IllegalArgumentException(
                        "Unsupported browser '" + browserName + "'. Use chromium, firefox, or webkit.");
            };
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.setDefaultTimeout(10_000);
            page.setDefaultNavigationTimeout(30_000);
            SESSION.set(new BrowserSession(playwright, browser, context, page, browserName));
        } catch (RuntimeException exception) {
            playwright.close();
            throw exception;
        }
    }

    public static Page page() {
        BrowserSession session = SESSION.get();
        if (session == null) {
            throw new IllegalStateException("The browser has not been launched for this scenario.");
        }
        return session.page();
    }

    public static boolean isStarted() {
        return SESSION.get() != null;
    }

    public static void close() {
        BrowserSession session = SESSION.get();
        SESSION.remove();
        if (session == null) {
            return;
        }
        try {
            session.context().close();
        } finally {
            try {
                session.browser().close();
            } finally {
                session.playwright().close();
            }
        }
    }

    private record BrowserSession(
            Playwright playwright,
            Browser browser,
            BrowserContext context,
            Page page,
            String browserName) {
    }
}
