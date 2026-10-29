package com.automation.api.base;

import com.automation.api.filters.ApiLoggingFilter;
import com.automation.util.TestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

public abstract class BaseService {
    protected RequestSpecification request() {
        return RestAssured.given()
                .baseUri(TestConfig.apiBaseUrl())
                .accept(ContentType.JSON)
                .filter(new ApiLoggingFilter());
    }

    protected Response getRequest(String endpoint) {
        return request().get(endpoint);
    }

    protected Response getRequest(String endpoint, Map<String, String> queryParameters) {
        return request().queryParams(queryParameters).get(endpoint);
    }

    protected Response postRequest(String endpoint, Object payload) {
        return request()
                .contentType(ContentType.JSON)
                .body(payload)
                .post(endpoint);
    }

    protected Response putRequest(String endpoint, String token, Object payload) {
        return authenticatedRequest(token)
                .contentType(ContentType.JSON)
                .body(payload)
                .put(endpoint);
    }

    protected Response patchRequest(String endpoint, String token, Object payload) {
        return authenticatedRequest(token)
                .contentType(ContentType.JSON)
                .body(payload)
                .patch(endpoint);
    }

    protected Response deleteRequest(String endpoint, String token) {
        return authenticatedRequest(token)
                .accept(ContentType.TEXT)
                .delete(endpoint);
    }

    private RequestSpecification authenticatedRequest(String token) {
        return request().header("Cookie", "token=" + token);
    }
}
