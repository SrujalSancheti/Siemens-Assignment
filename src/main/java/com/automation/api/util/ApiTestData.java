package com.automation.api.util;

import com.automation.api.models.request.AuthenticationRequest;
import com.automation.api.models.request.BookingDates;
import com.automation.api.models.request.BookingRequest;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiTestData {
    private ApiTestData() {
    }

    public static AuthenticationRequest authentication(String rowKey) {
        return new AuthenticationRequest(
                ApiExcelUtility.getValue(rowKey, "apiUsername"),
                ApiExcelUtility.getValue(rowKey, "apiPassword"));
    }

    public static BookingRequest booking(String rowKey) {
        return new BookingRequest(
                ApiExcelUtility.getValue(rowKey, "firstName"),
                ApiExcelUtility.getValue(rowKey, "lastName"),
                Integer.parseInt(ApiExcelUtility.getValue(rowKey, "totalPrice")),
                Boolean.parseBoolean(ApiExcelUtility.getValue(rowKey, "depositPaid")),
                new BookingDates(
                        ApiExcelUtility.getValue(rowKey, "checkin"),
                        ApiExcelUtility.getValue(rowKey, "checkout")),
                ApiExcelUtility.getOptionalValue(rowKey, "additionalNeeds"));
    }

    public static Map<String, String> bookingFilters(String rowKey, String combination) {
        Map<String, String> filters = new LinkedHashMap<>();
        for (String filter : combination.split("\\+")) {
            switch (filter) {
                case "firstName" -> filters.put("firstname", ApiExcelUtility.getValue(rowKey, "firstName"));
                case "lastName" -> filters.put("lastname", ApiExcelUtility.getValue(rowKey, "lastName"));
                case "checkin" -> filters.put("checkin", ApiExcelUtility.getValue(rowKey, "checkin"));
                case "checkout" -> filters.put("checkout", ApiExcelUtility.getValue(rowKey, "checkout"));
                default -> throw new IllegalArgumentException(
                        "Unsupported booking filter '" + filter + "' in combination '" + combination + "'.");
            }
        }
        return filters;
    }
}
