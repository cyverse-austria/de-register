package com.cyverse.keycloak.http;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ListenerHttpClient {

    private String host;
    private static final String HEALTH_ENDPOINT = "/";

    // TODO Auth
    public ListenerHttpClient(String host) {
        this.host = host;
    }

    public HttpClient getHttpClient() {
        return java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public HttpRequest getRequestPOST(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public HttpRequest getRequestPUT(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private HttpRequest getRequestHealthy() {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + HEALTH_ENDPOINT))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
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
