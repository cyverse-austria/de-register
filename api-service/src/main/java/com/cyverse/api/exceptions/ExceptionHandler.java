package com.cyverse.api.exceptions;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.util.Map;

/**
 * Exception handling in Javalin context.
 */
public class ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public static void handle(Javalin app) {
        // HTTP exceptions
        app.exception(UnauthorizedAccessException.class, (e, ctx) -> {
            String msg = String.format("Forbidden access: %s", e.getMessage());
            logger.error(msg);
            ctx.status(HttpStatus.UNAUTHORIZED);
            ctx.json(writeJsonErr(msg));
        });

        app.exception(JWTVerificationException.class, (e, ctx) -> {
            String msg = String.format("Failed JWT verification: %s", e.getMessage());
            logger.error(msg);
            ctx.status(HttpStatus.UNAUTHORIZED);
            ctx.json(writeJsonErr(msg));
        });

        app.exception(ResourceAlreadyExistsException.class, (e, ctx) -> {
            logger.warn("Resource already exists: {}", e.getMessage());
            ctx.status(HttpStatus.CONFLICT);
        });

        app.exception(UserException.class, (e, ctx) -> {
            String msg = String.format("Bad request error: %s", e.getMessage());
            logger.error(msg);
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(writeJsonErr(msg));
        });

        app.exception(IrodsException.class, (e, ctx) -> {
            String msg = String.format("iRODS Error: %s", e.getMessage());
            logger.error(msg);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(writeJsonErr(msg));
        });

        app.exception(NamingException.class, (e, ctx) -> {
            String msg = String.format("LDAP Error: %s", e.getMessage());
            logger.error(msg);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(writeJsonErr(msg));
        });

        app.exception(Exception.class, (e, ctx) -> {
            String msg = String.format("Internal server error: %s", e.getMessage());
            logger.error(msg);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(writeJsonErr(msg));
        });
    }

    private static String writeJsonErr(String err) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = Map.of("error", err);

        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
           return "Failed to write error as json: " + err;
        }
    }
}
