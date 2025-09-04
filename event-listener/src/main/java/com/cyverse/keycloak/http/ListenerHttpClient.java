package com.cyverse.keycloak.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public interface ListenerHttpClient {
    HttpClient getHttpClient();
    HttpRequest getRequestPOST(String endpoint, String body);
    HttpRequest getRequestPUT(String endpoint, String body);
    Boolean testConnection() throws IOException, InterruptedException ;
}
