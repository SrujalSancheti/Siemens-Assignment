package com.automation.stepdefinitions.ui;

import com.automation.ui.pages.CartPage;
import com.automation.util.BrowserManager;
import com.automation.util.ExcelUtility;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CartStepDefinitions {
    @Then("user validates product from Excel row {string} column {string} in cart")
    public void userValidatesProductFromExcelInCart(String rowKey, String column) {
        cartPage().assertProductPresent(ExcelUtility.getValue(rowKey, column));
    }

    @When("user removes product from Excel row {string} column {string} from cart")
    public void userRemovesProductFromExcelFromCart(String rowKey, String column) {
        cartPage().removeProduct(ExcelUtility.getValue(rowKey, column));
    }

    @Then("user validates the cart is empty")
    public void userValidatesTheCartIsEmpty() {
        cartPage().assertCount("cartItems", 0);
    }

    private CartPage cartPage() {
        return new CartPage(BrowserManager.page());
    }
}
