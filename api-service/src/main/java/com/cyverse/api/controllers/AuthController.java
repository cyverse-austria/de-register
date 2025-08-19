package com.cyverse.api.controllers;

import com.cyverse.api.config.AuthUserConfig;
import com.cyverse.api.exceptions.UnauthorizedAccessException;
import com.cyverse.api.services.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;

import java.util.Map;

import static com.cyverse.api.services.AuthService.EXPIRES_IN_MS;

public class AuthController {
    private AuthService authService;
    private String apiKey;
    private static final String API_KEY_HEADER = "X-API-KEY";

    public AuthController(AuthService authService, String apiKey) {
        this.authService = authService;
        this.apiKey = apiKey;
    }

    @OpenApi(
        summary = "Get a JWT token valid for 1 hour",
        operationId = "login",
        path = "/api/login",
        methods = HttpMethod.POST,
        security = @OpenApiSecurity(name = "ApiKeyAuth"),
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = AuthUserConfig.class)}
        ),
        headers = {
                @OpenApiParam(name = API_KEY_HEADER, required = true)
        },
        responses = {@OpenApiResponse(status = "200")}
    )
    public void login(Context ctx) throws UnauthorizedAccessException,
            JsonProcessingException {
        String authHeader = ctx.header(API_KEY_HEADER);
        checkApiKey(authHeader);

        AuthUserConfig user = ctx.bodyAsClass(AuthUserConfig.class);
        String generatedToken = authService.generateToken(user.getMail(), user.getPassword());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = Map.of(
                "jwt", generatedToken,
                "expires", EXPIRES_IN_MS
        );
        String jsonBody = mapper.writeValueAsString(data);

        ctx.status(HttpStatus.OK);
        ctx.json(jsonBody);
    }

    private void checkApiKey(String authHeader) throws UnauthorizedAccessException {
        if (authHeader == null || authHeader.isEmpty()) {
            throw new UnauthorizedAccessException("Missing api-key Authorization header");
        }

        if (!authHeader.equals(apiKey)) {
            throw new UnauthorizedAccessException("Invalid api-key in Authorization header");
        }
    }
}
