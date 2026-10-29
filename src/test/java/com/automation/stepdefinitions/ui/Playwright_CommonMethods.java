package com.automation.stepdefinitions.ui;

import com.automation.ui.pages.CommonPage;
import com.automation.ui.pages.LoginPage;
import com.automation.util.BrowserManager;
import com.automation.util.ExcelUtility;
import com.automation.util.TestConfig;
import com.automation.util.UiScreenshotManager;
import com.microsoft.playwright.Page;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Playwright_CommonMethods {
    @Given("user launches {string} browser")
    public void userLaunchesBrowser(String browserName) {
        String selectedBrowser = browserName.equalsIgnoreCase("configured")
                ? TestConfig.browserName()
                : browserName;
        BrowserManager.start(selectedBrowser);
    }

    @Before
    public void resetBrowserSession(Scenario scenario) {
        BrowserManager.close();
        UiScreenshotManager.clear();
        if (scenario.getSourceTagNames().contains("@ui")) {
            String uriPath = scenario.getUri().getPath();
            String featureName = uriPath == null ? "feature"
                    : uriPath.substring(uriPath.lastIndexOf('/') + 1).replaceFirst("\\.feature$", "");
            UiScreenshotManager.start(featureName, scenario.getName(), scenario.getId());
        }
    }

    @After
    public void closeBrowserSession(Scenario scenario) {
        try {
            if (BrowserManager.isStarted()) {
                UiScreenshotManager.capturePost(page());
                if (scenario.isFailed()) {
                    scenario.attach(
                            UiScreenshotManager.screenshotBytes(page()),
                            "image/png",
                            "failure");
                }
            }
        } finally {
            BrowserManager.close();
            UiScreenshotManager.clear();
        }
    }

    @When("user navigates to Sauce Demo URL")
    public void userNavigatesToSauceDemoUrl() {
        loginPage().open(TestConfig.baseUrl());
        UiScreenshotManager.capturePre(page());
    }

    @Then("user validates title {string} for {string}")
    public void userValidatesTitle(String expected, String locatorKey) {
        commonPage().assertText(locatorKey, expected);
    }

    @Then("user validates field {string}")
    public void userValidatesField(String locatorKey) {
        commonPage().assertVisible(locatorKey);
    }

    @Then("user validates {string} button")
    public void userValidatesButton(String locatorKey) {
        commonPage().assertVisible(locatorKey);
        commonPage().assertEnabled(locatorKey);
    }

    @When("user enters Excel data {string} column {string} into field {string}")
    public void userEntersExcelDataIntoField(String rowKey, String column, String locatorKey) {
        String value = ExcelUtility.getValue(rowKey, column);
        commonPage().fill(locatorKey, value);
    }

    @When("user clicks button {string}")
    public void userClicksButton(String locatorKey) {
        commonPage().click(locatorKey);
    }

    @Then("user validates text from Excel row {string} column {string} for {string}")
    public void userValidatesTextFromExcel(String rowKey, String column, String locatorKey) {
        commonPage().assertContainsText(locatorKey, ExcelUtility.getValue(rowKey, column));
    }

    @Then("user validates {int} elements for {string}")
    public void userValidatesElementCount(int expected, String locatorKey) {
        commonPage().assertCount(locatorKey, expected);
    }

    @Then("user validates element count from Excel row {string} column {string} for {string}")
    public void userValidatesElementCountFromExcel(String rowKey, String column, String locatorKey) {
        int expected = Integer.parseInt(ExcelUtility.getValue(rowKey, column));
        commonPage().assertCount(locatorKey, expected);
    }

    @Then("user validates that the login page is displayed")
    public void userValidatesThatLoginPageIsDisplayed() {
        commonPage().assertPageUrl(TestConfig.baseUrl() + "/");
        commonPage().assertVisible("loginButton");
    }

    @When("user selects Excel data {string} column {string} from dropdown {string}")
    public void userSelectsExcelDataFromDropdown(String rowKey, String column, String locatorKey) {
        commonPage().selectOption(locatorKey, ExcelUtility.getValue(rowKey, column));
    }

    private Page page() {
        return BrowserManager.page();
    }

    private LoginPage loginPage() {
        return new LoginPage(page());
    }

    private CommonPage commonPage() {
        return new CommonPage(page());
    }
}
