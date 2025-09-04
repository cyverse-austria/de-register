package com.cyverse.keycloak.http;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

public abstract class ListenerHttpClientBase implements ListenerHttpClient {

    protected String host;
    protected static final String HEALTH_ENDPOINT = "/";

    @Override
    public HttpClient getHttpClient() {
        return java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public Boolean testConnection() throws IOException, InterruptedException {
        HttpResponse<Void> response =
                getHttpClient()
                        .send(getRequestHealthy(),
                                HttpResponse.BodyHandlers.discarding());
        return response.statusCode() == HttpStatus.SC_OK;
    }

    private HttpRequest getRequestHealthy() {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + HEALTH_ENDPOINT))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .GET()
                .build();
    }
}
