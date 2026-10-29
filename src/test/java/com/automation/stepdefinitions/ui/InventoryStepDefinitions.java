package com.automation.stepdefinitions.ui;

import com.automation.ui.pages.InventoryPage;
import com.automation.util.BrowserManager;
import com.automation.util.ExcelUtility;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class InventoryStepDefinitions {
    @Then("products are sorted by price from low to high")
    public void productsAreSortedByPriceFromLowToHigh() {
        inventoryPage().assertPricesSortedLowToHigh();
    }

    @When("user adds product from Excel row {string} column {string} to cart")
    public void userAddsProductFromExcelToCart(String rowKey, String column) {
        inventoryPage().addProductToCart(ExcelUtility.getValue(rowKey, column));
    }

    @When("user opens the cart")
    public void userOpensTheCart() {
        inventoryPage().openCart();
    }

    @When("user logs out")
    public void userLogsOut() {
        inventoryPage().logout();
    }

    private InventoryPage inventoryPage() {
        return new InventoryPage(BrowserManager.page());
    }
}
