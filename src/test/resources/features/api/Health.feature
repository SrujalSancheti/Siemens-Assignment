@api @healthCheckApi
Feature: Ping health check

  Scenario: API is healthy
    When the API client checks the health endpoint
    Then the API response status is 201
    And the API response content type contains "text/plain"
