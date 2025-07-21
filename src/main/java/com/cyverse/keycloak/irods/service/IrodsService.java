package com.cyverse.keycloak.irods.service;

import com.cyverse.keycloak.irods.config.IrodsConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class IrodsService {
    private static final Logger logger = Logger.getLogger(IrodsService.class);
    private final IrodsConfig config;

    // TODO Auth
    public IrodsService(IrodsConfig config) {
        this.config = config;
    }

    private HttpClient getHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    private HttpRequest getRequestPOST(String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(config.getIrodsHost() + "/api/users"))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public void addIrodsUser(UserModel user) {
        logger.debug("Try adding user to iRODS: " + user.getUsername());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = Map.of(
                "username", user.getUsername()
        );

        try {
            String jsonBody = mapper.writeValueAsString(data);
            HttpResponse<String> response = getHttpClient().send(getRequestPOST(jsonBody), HttpResponse.BodyHandlers.ofString());
            logger.debug("iRODS Client RESPONSE STATUS CODE: " + response.statusCode());

            if (response.statusCode() == HttpStatus.SC_CREATED) {
                logger.info("Successfully added user " + user.getUsername() + " to iRODS");
            }
            logger.debug("iRODS Client RESPONSE BODY: " + response.body());
        } catch (JsonProcessingException jsonExc) {
            logger.error("Got exception trying to build iRODS client body data: " + user.getUsername() + "\n" + jsonExc.getMessage());
        } catch (IOException | InterruptedException httpExc) {
            logger.error("Got exception from HTTP request to iRODS client: " + httpExc.getMessage());
        }
    }
}
