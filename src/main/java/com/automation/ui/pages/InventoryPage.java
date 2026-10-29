package com.automation.ui.pages;

import com.automation.util.JsonUtility;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import java.util.Comparator;
import java.util.List;

public final class InventoryPage extends BasePage {
    public InventoryPage(Page page) {
        super(page);
    }

    public void selectSortOption(String option) {
        locator("sortDropdown").selectOption(option);
    }

    public void assertPricesSortedLowToHigh() {
        Locator prices = locator("productPrices");
        List<String> displayedPrices = prices.allTextContents().stream()
                .map(String::trim)
                .toList();
        List<String> sortedPrices = displayedPrices.stream()
                .sorted(Comparator.comparingDouble(InventoryPage::priceValue))
                .toList();

        for (int index = 0; index < sortedPrices.size(); index++) {
            PlaywrightAssertions.assertThat(prices.nth(index))
                    .hasText(exactTextPattern(sortedPrices.get(index)));
        }
    }

    public void addProductToCart(String productName) {
        Locator product = locator("productByName")
                .filter(new Locator.FilterOptions().setHasText(productName));
        Locator addButton = product.locator(JsonUtility.selector("addProductButton"));
        PlaywrightAssertions.assertThat(addButton).hasCount(1);
        addButton.click();
    }

    public void openCart() {
        locator("cartLink").click();
    }

    public void logout() {
        locator("menuButton").click();
        locator("logoutLink").click();
    }

    private static double priceValue(String price) {
        return Double.parseDouble(price.replace("$", "").trim());
    }
}
