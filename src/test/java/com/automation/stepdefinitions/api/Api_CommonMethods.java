package com.automation.stepdefinitions.api;

import com.automation.api.models.request.BookingRequest;
import com.automation.api.models.response.AuthenticationResponse;
import com.automation.api.models.response.BookingResponse;
import com.automation.api.services.AuthenticationService;
import com.automation.api.services.BookingService;
import com.automation.api.services.HealthCheckService;
import com.automation.api.util.ApiExcelUtility;
import com.automation.api.util.ApiTestData;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.testng.Assert;

public class Api_CommonMethods {
    private final AuthenticationService authenticationService = new AuthenticationService();
    private final BookingService bookingService = new BookingService();
    private final HealthCheckService healthCheckService = new HealthCheckService();
    private Response response;
    private String token;
    private Integer bookingId;

    @When("the API client checks the health endpoint")
    public void checkHealthEndpoint() {
        response = healthCheckService.checkHealth();
    }

    @When("the API client requests the booking list")
    public void requestBookingList() {
        response = bookingService.getBookings();
    }

    @Then("the booking list contains at least one booking")
    public void bookingListContainsBooking() {
        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        Assert.assertNotNull(ids, "Booking list should contain booking IDs");
        Assert.assertFalse(ids.isEmpty(), "Booking list should not be empty");
    }

    @When("the API client authenticates with Excel row {string}")
    public void authenticate(String rowKey) {
        response = authenticationService.createToken(ApiTestData.authentication(rowKey));
        if (response.statusCode() == 200) {
            token = response.as(AuthenticationResponse.class).getToken();
        }
    }

    @When("the API client tries authentication with Excel row {string}")
    public void tryAuthentication(String rowKey) {
        response = authenticationService.createToken(ApiTestData.authentication(rowKey));
    }

    @Then("the API response status is {int}")
    public void responseStatusIs(int expectedStatus) {
        Assert.assertEquals(response.statusCode(), expectedStatus,
                "Unexpected API response body: " + response.asString());
    }

    @Then("the API authentication token is present")
    public void authenticationTokenIsPresent() {
        Assert.assertNotNull(token, "Successful authentication must return a token");
        Assert.assertFalse(token.isBlank(), "Successful authentication token must not be blank");
    }

    @Then("the API authentication reason matches Excel row {string}")
    public void authenticationReasonMatchesExcel(String rowKey) {
        AuthenticationResponse authentication = response.as(AuthenticationResponse.class);
        Assert.assertEquals(authentication.getReason(), ApiExcelUtility.getValue(rowKey, "expectedMessage"));
    }

    @When("the API client creates a booking from Excel row {string}")
    public void createBooking(String rowKey) {
        response = bookingService.createBooking(ApiTestData.booking(rowKey));
        if (response.statusCode() == 200) {
            bookingId = response.as(BookingResponse.class).getBookingid();
        }
    }

    @Then("the API response contains the created booking")
    public void responseContainsCreatedBooking() {
        Assert.assertNotNull(bookingId, "Booking creation must return a booking ID");
        BookingResponse bookingResponse = response.as(BookingResponse.class);
        Assert.assertNotNull(bookingResponse.getBooking(), "Booking creation must return the booking payload");
    }

    @Then("the API booking matches Excel row {string}")
    public void bookingMatchesExcel(String rowKey) {
        BookingRequest actual;
        if (response.jsonPath().get("booking") != null) {
            actual = response.as(BookingResponse.class).getBooking();
        } else {
            actual = response.as(BookingRequest.class);
        }
        assertBookingMatches(actual, ApiTestData.booking(rowKey));
    }

    @When("the API client retrieves the created booking")
    public void retrieveCreatedBooking() {
        response = bookingService.getBooking(requiredBookingId());
    }

    @When("the API client searches the created booking using filter combination {string} from Excel row {string}")
    public void searchCreatedBooking(String combination, String rowKey) {
        response = bookingService.getBookings(ApiTestData.bookingFilters(rowKey, combination));
    }

    @Then("the API booking search results include the created booking")
    public void bookingSearchIncludesCreatedBooking() {
        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        Assert.assertNotNull(ids, "Booking search should return a list of booking IDs");
        Assert.assertTrue(ids.contains(requiredBookingId()),
                "Filtered booking results should contain the booking created by this scenario");
    }

    @When("the API client replaces the created booking using Excel row {string}")
    public void replaceCreatedBooking(String rowKey) {
        response = bookingService.replaceBooking(
                requiredBookingId(), requiredToken(), ApiTestData.booking(rowKey));
    }

    @When("the API client partially updates the created booking")
    public void partiallyUpdateCreatedBooking() {
        response = bookingService.partiallyUpdateBooking(
                requiredBookingId(),
                requiredToken(),
                Map.of("additionalneeds", "Late checkout - updated by API test"));
    }

    @When("the API client tries to delete the created booking with an invalid token")
    public void deleteWithInvalidToken() {
        response = bookingService.deleteBooking(requiredBookingId(), "invalid-token");
    }

    @When("the API client deletes the created booking")
    public void deleteCreatedBooking() {
        response = bookingService.deleteBooking(requiredBookingId(), requiredToken());
        if (response.statusCode() == 201) {
            bookingId = null;
        }
    }

    @When("the API client requests booking ID {int}")
    public void requestBookingId(int id) {
        response = bookingService.getBooking(id);
    }

    @Then("the API response content type contains {string}")
    public void responseContentTypeContains(String expected) {
        Assert.assertTrue(response.getContentType().contains(expected),
                "Expected content type to contain '" + expected + "'");
    }

    @Then("the API response has an authentication error")
    public void responseHasAuthenticationError() {
        Assert.assertEquals(response.asString().trim(), "Forbidden");
    }

    @Then("the API response confirms the booking still exists")
    public void responseConfirmsBookingExists() {
        Assert.assertEquals(response.statusCode(), 200);
        BookingRequest actual = response.as(BookingRequest.class);
        Assert.assertEquals(actual.getFirstname(), ApiExcelUtility.getValue("apiBooking", "firstName"));
    }

    @After("@api")
    public void cleanUpTestBooking() {
        if (bookingId != null && token != null && !token.isBlank()) {
            Response cleanupResponse = bookingService.deleteBooking(bookingId, token);
            Assert.assertEquals(cleanupResponse.statusCode(), 201,
                    "Cleanup should delete only the booking created by this scenario");
            bookingId = null;
        }
    }

    private void assertBookingMatches(BookingRequest actual, BookingRequest expected) {
        Assert.assertNotNull(actual, "API response should include a booking");
        Assert.assertEquals(actual.getFirstname(), expected.getFirstname());
        Assert.assertEquals(actual.getLastname(), expected.getLastname());
        Assert.assertEquals(actual.getTotalprice(), expected.getTotalprice());
        Assert.assertEquals(actual.isDepositpaid(), expected.isDepositpaid());
        Assert.assertEquals(actual.getBookingdates().getCheckin(), expected.getBookingdates().getCheckin());
        Assert.assertEquals(actual.getBookingdates().getCheckout(), expected.getBookingdates().getCheckout());
        Assert.assertEquals(actual.getAdditionalneeds(), expected.getAdditionalneeds());
    }

    private int requiredBookingId() {
        if (bookingId == null) {
            throw new IllegalStateException("This API scenario has not created a booking.");
        }
        return bookingId;
    }

    private String requiredToken() {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Authenticate before performing a protected booking operation.");
        }
        return token;
    }
}
