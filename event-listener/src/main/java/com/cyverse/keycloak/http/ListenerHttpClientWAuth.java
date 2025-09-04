package com.cyverse.keycloak.http;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

public class ListenerHttpClientWAuth extends ListenerHttpClientBase {

    private String apiKey;
    private TokenService tokenService;

    public ListenerHttpClientWAuth(String host, String apiKey, TokenService tokenService) {
        this.host = host;
        this.apiKey = apiKey;
        this.tokenService = tokenService;
    }

    @Override
    public HttpRequest getRequestPOST(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .header(AUTHORIZATION, "Bearer " + tokenService.getToken(this))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    @Override
    public HttpRequest getRequestPUT(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .header(AUTHORIZATION, "Bearer " +  tokenService.getToken(this))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public HttpRequest getRequestPOSTApiKey(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .header("X-API-KEY", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }
}
