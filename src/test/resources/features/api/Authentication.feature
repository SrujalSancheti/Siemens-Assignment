@api @apiAuthentication
Feature: Restful Booker authentication

  Scenario: Valid API credentials return a token
    When the API client authenticates with Excel row "apiAuth"
    Then the API response status is 200
    And the API authentication token is present

  Scenario Outline: Invalid credential combinations are rejected
    When the API client tries authentication with Excel row "<dataKey>"
    Then the API response status is 200
    And the API authentication reason matches Excel row "<dataKey>"

    Examples:
      | dataKey                 |
      | apiInvalidUsername      |
      | apiInvalidPassword      |
