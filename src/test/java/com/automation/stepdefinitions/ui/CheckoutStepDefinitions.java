package com.automation.stepdefinitions.ui;

import com.automation.ui.pages.CartPage;
import com.automation.ui.pages.CheckoutPage;
import com.automation.util.BrowserManager;
import com.automation.util.ExcelUtility;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CheckoutStepDefinitions {
    @When("user completes checkout using Excel row {string}")
    public void userCompletesCheckoutUsingExcelRow(String rowKey) {
        cartPage().checkout();
        checkoutPage().enterCustomerDetails(
                ExcelUtility.getValue(rowKey, "firstName"),
                ExcelUtility.getValue(rowKey, "lastName"),
                ExcelUtility.getValue(rowKey, "postalCode"));
    }

    @Then("user validates the checkout overview")
    public void userValidatesTheCheckoutOverview() {
        checkoutPage().assertOverviewDisplayed();
    }

    @When("user completes the order")
    public void userCompletesTheOrder() {
        checkoutPage().finishOrder();
    }

    @Then("user validates the order confirmation using Excel row {string}")
    public void userValidatesOrderConfirmation(String rowKey) {
        checkoutPage().assertText(
                "confirmationHeader",
                ExcelUtility.getValue(rowKey, "expectedConfirmation"));
    }

    private CartPage cartPage() {
        return new CartPage(BrowserManager.page());
    }

    private CheckoutPage checkoutPage() {
        return new CheckoutPage(BrowserManager.page());
    }
}
