package com.cyverse.irods.exceptions;

import com.cyverse.irods.controllers.IrodsController;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public static void handle(Javalin app) {
        // HTTP exceptions
        app.exception(IrodsController.IrodsException.class, (e, ctx) -> {
            logger.error("Bad request error: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST);
        });

        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Internal server error: {}", e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }
}
