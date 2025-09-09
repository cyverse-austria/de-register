package com.cyverse.keycloak.notification.service;

import com.cyverse.keycloak.http.ListenerHttpClientBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Notification Service in Keycloak context.
 */
public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = Logger.getLogger(NoOpNotificationServiceImpl.class);
    private final ListenerHttpClientBase httpClient;

    private static final String NOTIFICATION_USERS_ENDPOINT = "/api/users/notification";

    public NotificationServiceImpl(ListenerHttpClientBase httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void notifyUser(UserModel user) {
        logger.debug("Try notifying user: " + user.getUsername());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = Map.of(
                "username", user.getUsername(),
                "email", user.getEmail()
        );

        try {
            String jsonBody = mapper.writeValueAsString(data);

            HttpResponse<String> response =
                    httpClient.getHttpClient()
                            .send(httpClient
                                            .getRequestPUT(NOTIFICATION_USERS_ENDPOINT, jsonBody),
                                    HttpResponse.BodyHandlers.ofString());

            logger.debug("API RESPONSE STATUS CODE: " + response.statusCode());

            if (response.statusCode() == HttpStatus.SC_OK) {
                logger.info("Successfully notified user " + user.getUsername());
            }
        } catch (JsonProcessingException jsonExc) {
            logger.error("Got exception trying to build API client body data: " + user.getUsername() + "\n" + jsonExc.getMessage());
        } catch (IOException | InterruptedException httpExc) {
            logger.error("Got exception from HTTP request to API client: " + httpExc.getMessage());
        }
    }
}
