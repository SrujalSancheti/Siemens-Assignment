package com.automation.ui.pages;

import com.automation.util.JsonUtility;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import java.util.regex.Pattern;

public abstract class BasePage {
    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected Locator locator(String key) {
        return JsonUtility.locator(page, key);
    }

    protected Locator locator(String key, String value) {
        return JsonUtility.locator(page, key, value);
    }

    public void assertText(String key, String expected) {
        PlaywrightAssertions.assertThat(locator(key))
                .hasText(exactTextPattern(expected));
    }

    public void assertContainsText(String key, String expected) {
        PlaywrightAssertions.assertThat(locator(key)).containsText(expected);
    }

    public void assertVisible(String key) {
        PlaywrightAssertions.assertThat(locator(key)).isVisible();
    }

    public void assertEnabled(String key) {
        PlaywrightAssertions.assertThat(locator(key)).isEnabled();
    }

    public void assertCount(String key, int expected) {
        PlaywrightAssertions.assertThat(locator(key)).hasCount(expected);
    }

    public void assertPageUrl(String expectedUrl) {
        PlaywrightAssertions.assertThat(page).hasURL(expectedUrl);
    }

    protected static Pattern exactTextPattern(String expected) {
        StringBuilder escaped = new StringBuilder();
        for (char character : expected.toCharArray()) {
            if ("\\.^$|?*+()[]{}".indexOf(character) >= 0) {
                escaped.append('\\');
            }
            escaped.append(character);
        }
        return Pattern.compile("^\\s*" + escaped + "\\s*$");
    }
}
