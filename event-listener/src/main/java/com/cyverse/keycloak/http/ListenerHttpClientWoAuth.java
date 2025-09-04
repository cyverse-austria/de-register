package com.cyverse.keycloak.http;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

public class ListenerHttpClientWoAuth extends ListenerHttpClientBase {

    public ListenerHttpClientWoAuth(String host) {
        this.host = host;
    }

    @Override
    public HttpRequest getRequestPOST(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    @Override
    public HttpRequest getRequestPUT(String endpoint, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(host + endpoint))
                .timeout(Duration.ofMinutes(1))
                .header(CONTENT_TYPE, "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }
}
