package com.automation.api.services;

import com.automation.api.base.BaseService;
import io.restassured.response.Response;

public class HealthCheckService extends BaseService {
    public Response checkHealth() {
        return getRequest("/ping");
    }
}
