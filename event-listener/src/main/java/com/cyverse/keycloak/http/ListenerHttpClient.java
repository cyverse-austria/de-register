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

public class ListenerHttpClient {

    private String host;
    private String apiKey;
    private TokenService tokenService;

    private static final String HEALTH_ENDPOINT = "/";

    public ListenerHttpClient(String host, String apiKey, TokenService tokenService) {
        this.host = host;
        this.apiKey = apiKey;
        this.tokenService = tokenService;
    }

    public HttpClient getHttpClient() {
        return java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public HttpRequest getRequestPOSTnoAuth(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public HttpRequest getRequestPOST(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .header(AUTHORIZATION, "Bearer " + tokenService.getToken(this))
                .POST(HttpRequest.BodyPublishers.ofString(body))
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

    public HttpRequest getRequestPUTnoAuth(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public HttpRequest getRequestPUT(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .header(AUTHORIZATION, "Bearer " +  tokenService.getToken(this))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private HttpRequest getRequestHealthy() {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + HEALTH_ENDPOINT))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .GET()
                .build();
    }

    public Boolean testConnection() throws IOException, InterruptedException {
        HttpResponse<Void> response =
                getHttpClient()
                        .send(getRequestHealthy(),
                                HttpResponse.BodyHandlers.discarding());
        return response.statusCode() == HttpStatus.SC_OK;
    }
}
