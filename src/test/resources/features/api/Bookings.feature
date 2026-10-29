@api @apiBookings
Feature: Restful Booker bookings API

  Scenario: Booking list can be retrieved
    When the API client requests the booking list
    Then the API response status is 200
    And the API response content type contains "application/json"
    And the booking list contains at least one booking

  @stateful
  Scenario: A booking can be retrieved using every documented filter combination
    Given the API client authenticates with Excel row "apiAuth"
    And the API client creates a booking from Excel row "apiBooking"
    Then the API response status is 200
    When the API client searches the created booking using filter combination "firstName" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "lastName" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "checkin" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "checkout" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "firstName+lastName" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "firstName+checkin" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "firstName+checkout" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "lastName+checkin" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "lastName+checkout" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "checkin+checkout" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "firstName+lastName+checkin" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "firstName+lastName+checkout" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "firstName+checkin+checkout" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "lastName+checkin+checkout" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking
    When the API client searches the created booking using filter combination "firstName+lastName+checkin+checkout" from Excel row "apiBooking"
    Then the API response status is 200
    And the API booking search results include the created booking

  @stateful
  Scenario Outline: A booking can be created with valid optional-field combinations
    Given the API client authenticates with Excel row "apiAuth"
    When the API client creates a booking from Excel row "<dataKey>"
    Then the API response status is 200
    And the API response contains the created booking
    And the API booking matches Excel row "<dataKey>"

    Examples:
      | dataKey              |
      | apiBooking           |
      | apiBookingNoDeposit  |

  @stateful
  Scenario: A booking can be read, replaced, partially updated, and deleted
    Given the API client authenticates with Excel row "apiAuth"
    When the API client creates a booking from Excel row "apiBooking"
    Then the API response status is 200
    When the API client retrieves the created booking
    Then the API response status is 200
    And the API booking matches Excel row "apiBooking"
    When the API client replaces the created booking using Excel row "apiBookingUpdated"
    Then the API response status is 200
    And the API booking matches Excel row "apiBookingUpdated"
    When the API client partially updates the created booking
    Then the API response status is 200
    And the API booking matches Excel row "apiBookingPatched"
    When the API client tries to delete the created booking with an invalid token
    Then the API response status is 403
    And the API response has an authentication error
    When the API client retrieves the created booking
    Then the API response confirms the booking still exists
    When the API client deletes the created booking
    Then the API response status is 201
    When the API client requests booking ID 0
    Then the API response status is 404

  Scenario Outline: Booking retrieval rejects unknown booking IDs
    When the API client requests booking ID <bookingId>
    Then the API response status is 404

    Examples:
      | bookingId |
      | 0         |
      | 99999999  |
