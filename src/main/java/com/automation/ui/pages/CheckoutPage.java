package com.automation.ui.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import java.util.regex.Pattern;

public final class CheckoutPage extends BasePage {
    public CheckoutPage(Page page) {
        super(page);
    }

    public void enterCustomerDetails(String firstName, String lastName, String postalCode) {
        locator("firstNameField").fill(firstName);
        locator("lastNameField").fill(lastName);
        locator("postalCodeField").fill(postalCode);
        locator("continueButton").click();
    }

    public void assertOverviewDisplayed() {
        assertVisible("checkoutSummary");
        PlaywrightAssertions.assertThat(locator("checkoutTotal"))
                .hasText(Pattern.compile("^Total: \\$.*"));
    }

    public void finishOrder() {
        locator("finishButton").click(new Locator.ClickOptions().setNoWaitAfter(true));
    }
}
