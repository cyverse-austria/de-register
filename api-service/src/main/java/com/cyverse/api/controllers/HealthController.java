package com.cyverse.api.controllers;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiResponse;

public class HealthController {

    @OpenApi(
        summary = "Get the health status of the app",
        operationId = "getHealthy",
        path = "/",
        methods = HttpMethod.GET,
        responses = {@OpenApiResponse(status = "200")}
    )
    public void getHealthy(Context ctx) {
        ctx.status(HttpStatus.OK);
    }
}
