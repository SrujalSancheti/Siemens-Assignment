@ui @login
Feature: Sauce Demo login

  Scenario: The login form is available
    Given user launches "chrome" browser
    When user navigates to Sauce Demo URL
    Then user validates title "Swag Labs" for "loginPageTitle"
    And user validates field "usernameField"
    And user validates field "passwordField"
    And user validates "loginButton" button

  Scenario: A standard user can log in
    Given user launches "chrome" browser
    When user navigates to Sauce Demo URL
    And user enters Excel data "standardUser" column "username" into field "usernameField"
    And user enters Excel data "standardUser" column "password" into field "passwordField"
    And user clicks button "loginButton"
    Then user validates title "Products" for "inventoryPageTitle"

  Scenario Outline: Invalid users cannot log in
    Given user launches "chrome" browser
    When user navigates to Sauce Demo URL
    And user enters Excel data "<dataKey>" column "username" into field "usernameField"
    And user enters Excel data "<dataKey>" column "password" into field "passwordField"
    And user clicks button "loginButton"
    Then user validates text from Excel row "<dataKey>" column "expectedMessage" for "loginError"

    Examples:
      | dataKey     |
      | invalidUser |
      | lockedUser  |
