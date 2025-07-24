package com.cyverse.api.controllers;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class HealthController {

    public void getHealthy(Context ctx) {
        ctx.status(HttpStatus.OK);
    }
}
