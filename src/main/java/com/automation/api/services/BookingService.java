package com.automation.api.services;

import com.automation.api.base.BaseService;
import com.automation.api.models.request.BookingRequest;
import io.restassured.response.Response;
import java.util.Map;

public class BookingService extends BaseService {
    private static final String BASE_PATH = "/booking";

    public Response getBookings() {
        return getRequest(BASE_PATH);
    }

    public Response getBookings(Map<String, String> filters) {
        return getRequest(BASE_PATH, filters);
    }

    public Response getBooking(int bookingId) {
        return getRequest(BASE_PATH + "/" + bookingId);
    }

    public Response createBooking(BookingRequest booking) {
        return postRequest(BASE_PATH, booking);
    }

    public Response replaceBooking(int bookingId, String token, BookingRequest booking) {
        return putRequest(BASE_PATH + "/" + bookingId, token, booking);
    }

    public Response partiallyUpdateBooking(int bookingId, String token, Map<String, Object> fields) {
        return patchRequest(BASE_PATH + "/" + bookingId, token, fields);
    }

    public Response deleteBooking(int bookingId, String token) {
        return deleteRequest(BASE_PATH + "/" + bookingId, token);
    }
}
