package com.automation.ui.pages;

import com.microsoft.playwright.Page;

public final class CommonPage extends BasePage {
    public CommonPage(Page page) {
        super(page);
    }

    public void fill(String locatorKey, String value) {
        locator(locatorKey).fill(value);
    }

    public void click(String locatorKey) {
        locator(locatorKey).click();
    }

    public void selectOption(String locatorKey, String value) {
        locator(locatorKey).selectOption(value);
    }
}
