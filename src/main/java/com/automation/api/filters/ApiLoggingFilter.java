package com.automation.api.filters;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.logging.Logger;

public class ApiLoggingFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    @Override
    public Response filter(
            FilterableRequestSpecification request,
            FilterableResponseSpecification responseSpecification,
            FilterContext context) {
        LOGGER.info(() -> "API request: " + request.getMethod() + " " + request.getURI());
        Response response = context.next(request, responseSpecification);
        LOGGER.info(() -> "API response: HTTP " + response.statusCode() + " for "
                + request.getMethod() + " " + request.getURI());
        return response;
    }
}
