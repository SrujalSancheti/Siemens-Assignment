package com.automation.api.services;

import com.automation.api.base.BaseService;
import com.automation.api.models.request.AuthenticationRequest;
import io.restassured.response.Response;

public class AuthenticationService extends BaseService {
    private static final String BASE_PATH = "/auth";

    public Response createToken(AuthenticationRequest request) {
        return postRequest(BASE_PATH, request);
    }
}
