package com.cyverse.keycloak.irods.service;

import com.cyverse.keycloak.http.ListenerHttpClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

public class IrodsService {
    private static final Logger logger = Logger.getLogger(IrodsService.class);
    private final ListenerHttpClient httpClient;
    private static final String IRODS_ENDPOINT = "/api/users/irods";

    public IrodsService(ListenerHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void addIrodsUser(UserModel user) {
        logger.debug("Try adding user to iRODS: " + user.getUsername());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = Map.of(
                "username", user.getUsername()
        );

        try {
            String jsonBody = mapper.writeValueAsString(data);

            HttpResponse<String> response =
                    httpClient.getHttpClient()
                            .send(httpClient
                                    .getRequestPOST(IRODS_ENDPOINT, jsonBody),
                                    HttpResponse.BodyHandlers.ofString());

            logger.debug("API RESPONSE STATUS CODE: " + response.statusCode());

            if (response.statusCode() == HttpStatus.SC_CREATED) {
                logger.info("Successfully added user " + user.getUsername() + " to iRODS");
            }
            logger.debug("API Client RESPONSE BODY: " + response.body());
        } catch (JsonProcessingException jsonExc) {
            logger.error("Got exception trying to build API body data: " + user.getUsername() + "\n" + jsonExc.getMessage());
        } catch (IOException | InterruptedException httpExc) {
            logger.error("Got exception from HTTP request to API: " + httpExc.getMessage());
        }
    }
}
