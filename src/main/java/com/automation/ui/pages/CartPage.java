package com.automation.ui.pages;

import com.automation.util.JsonUtility;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

public final class CartPage extends BasePage {
    public CartPage(Page page) {
        super(page);
    }

    public void assertProductPresent(String productName) {
        Locator product = locator("cartItemByName")
                .filter(new Locator.FilterOptions().setHasText(productName));
        PlaywrightAssertions.assertThat(product).isVisible();
    }

    public void removeProduct(String productName) {
        Locator product = locator("cartItemByName")
                .filter(new Locator.FilterOptions().setHasText(productName));
        product.locator(JsonUtility.selector("removeProductButton")).click();
    }

    public void checkout() {
        locator("checkoutButton").click();
    }
}
