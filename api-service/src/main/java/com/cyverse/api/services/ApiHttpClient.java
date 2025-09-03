package com.cyverse.api.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

/**
 * HTTP Client used inside API (HTTP server) to trigger other APIs.
 * For now without authentication.
 */
public class ApiHttpClient {

    private final String host;

    public ApiHttpClient(String host) {
        this.host = host;
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

    public HttpRequest getRequestPUTnoAuth(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public HttpRequest getRequestGETnoAuth(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .GET()
                .build();
    }
}
