package com.cyverse.keycloak.portal.service;

import com.cyverse.keycloak.http.ListenerHttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * User Portal Service with Keycloak context.
 */
public class UserPortalServiceImpl implements UserPortalService {
    private static final Logger logger = Logger.getLogger(UserPortalServiceImpl.class);
    private final ListenerHttpClient httpClient;

    private static final String PORTAL_USERS_ENDPOINT = "/api/users/portal";

    public UserPortalServiceImpl(ListenerHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Adds the user in the CyVerse User Portal DB via api-service.
     *
     * @param user the UserModel that comes from Keycloak data-model
     */
    public void addUserToPortal(UserModel user) {
        logger.debug("Try adding user to User Portal: " + user.getUsername());

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> data = Map.of(
                "username", user.getUsername(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail()
        );

        try {
            String jsonBody = mapper.writeValueAsString(data);

            HttpResponse<String> response =
                    httpClient.getHttpClient()
                            .send(httpClient
                                            .getRequestPOST(PORTAL_USERS_ENDPOINT, jsonBody),
                                    HttpResponse.BodyHandlers.ofString());

            logger.debug("API RESPONSE STATUS CODE: " + response.statusCode());

            if (response.statusCode() == HttpStatus.SC_OK) {
                logger.info("Successfully added user " + user.getUsername() + " in User Portal");
            }
        } catch (JsonProcessingException jsonExc) {
            logger.error("Got exception trying to build API client body data: " + user.getUsername() + "\n" + jsonExc.getMessage());
        } catch (IOException | InterruptedException httpExc) {
            logger.error("Got exception from HTTP request to API client: " + httpExc.getMessage());
        }
    }
}
