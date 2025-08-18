package com.cyverse.api.exceptions;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;

/**
 * Exception handling in Javalin context.
 */
public class ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public static void handle(Javalin app) {
        // HTTP exceptions
        app.exception(ResourceAlreadyExistsException.class, (e, ctx) -> {
            logger.warn("Resource already exists: {}", e.getMessage());
            ctx.status(HttpStatus.CONFLICT);
        });

        app.exception(UserException.class, (e, ctx) -> {
            String msg = String.format("Bad request error: %s", e.getMessage());
            logger.error(msg);
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(msg);
        });

        app.exception(IrodsException.class, (e, ctx) -> {
            String msg = String.format("iRODS Error: %s", e.getMessage());
            logger.error(msg);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(msg);
        });

        app.exception(NamingException.class, (e, ctx) -> {
            String msg = String.format("LDAP Error: %s", e.getMessage());
            logger.error(msg);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(msg);
        });

        app.exception(Exception.class, (e, ctx) -> {
            String msg = String.format("Internal server error: %s", e.getMessage());
            logger.error(msg);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(msg);
        });
    }
}
