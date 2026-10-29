@ui @shopping
Feature: Sauce Demo shopping and checkout

  Background:
    Given user launches "configured" browser
    When user navigates to Sauce Demo URL
    And user enters Excel data "standardUser" column "username" into field "usernameField"
    And user enters Excel data "standardUser" column "password" into field "passwordField"
    And user clicks button "loginButton"
    Then user validates title "Products" for "inventoryPageTitle"

  Scenario: Inventory products can be sorted by ascending price
    Then user validates element count from Excel row "sortAscending" column "expectedProductCount" for "productCount"
    When user selects Excel data "sortAscending" column "sortOption" from dropdown "sortDropdown"
    Then products are sorted by price from low to high

  Scenario: A product can be added to and removed from the cart
    When user adds product from Excel row "purchase" column "productName" to cart
    And user opens the cart
    Then user validates product from Excel row "purchase" column "productName" in cart
    When user removes product from Excel row "purchase" column "productName" from cart
    Then user validates the cart is empty

  Scenario: A user can place an order
    When user adds product from Excel row "purchase" column "productName" to cart
    And user opens the cart
    Then user validates product from Excel row "purchase" column "productName" in cart
    When user completes checkout using Excel row "purchase"
    Then user validates the checkout overview
    When user completes the order
    Then user validates the order confirmation using Excel row "purchase"

  Scenario: A user can log out
    When user logs out
    Then user validates that the login page is displayed
