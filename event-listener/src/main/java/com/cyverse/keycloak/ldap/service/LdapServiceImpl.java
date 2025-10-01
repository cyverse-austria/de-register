package com.cyverse.keycloak.ldap.service;

import com.cyverse.keycloak.http.ListenerHttpClientBase;
import com.cyverse.keycloak.http.ListenerHttpClientWAuth;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 *  LDAP Service in Keycloak context.
 */
public class LdapServiceImpl implements LdapService {
    private static final Logger logger = Logger.getLogger(LdapServiceImpl.class);
    private final ListenerHttpClientBase httpClient;

    private static final String LDAP_USERS_ENDPOINT = "/api/users/ldap";
    private static final String LDAP_GROUPS_ENDPOINT = "/api/groups/ldap";

    public LdapServiceImpl(ListenerHttpClientBase httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sends all user data to LDAP api.
     *
     * @param user the UserModel that comes from Keycloak data-model
     */
    @Override
    public void updateLdapUser(UserModel user) {
        logger.debug("Try adding user to LDAP: " + user.getUsername());

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
                                            .getRequestPUT(LDAP_USERS_ENDPOINT, jsonBody),
                                    HttpResponse.BodyHandlers.ofString());

            logger.debug("API RESPONSE STATUS CODE: " + response.statusCode());

            if (response.statusCode() == HttpStatus.SC_OK) {
                logger.info("Successfully updated user " + user.getUsername() + " in LDAP");
            } else {
                logger.warn(response.body());
            }
        } catch (JsonProcessingException jsonExc) {
            logger.error("Got exception trying to build API client body data: " + user.getUsername() + "\n" + jsonExc.getMessage());
        } catch (IOException | InterruptedException httpExc) {
            logger.error("Got exception from HTTP request to API client: " + httpExc.getMessage());
        }
    }

    /**
     * Adds already existing LDAP user to the specified group.
     *
     * @param user the user registered in LDAP
     * @param group the group to add the user to
     */
    @Override
    public void addLdapUserToGroup(UserModel user, String group) {
        logger.debug("Try adding user to LDAP: " + user.getUsername());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = Map.of(
                "username", user.getUsername(),
                "group", group
        );

        try {
            String jsonBody = mapper.writeValueAsString(data);

            HttpResponse<String> response =
                    httpClient.getHttpClient()
                            .send(httpClient
                                            .getRequestPUT(LDAP_GROUPS_ENDPOINT, jsonBody),
                                    HttpResponse.BodyHandlers.ofString());

            logger.debug("API RESPONSE STATUS CODE: " + response.statusCode());

            if (response.statusCode() == HttpStatus.SC_OK) {
                logger.info("Successfully added user " + user.getUsername() + " to LDAP group: " + group);
            } else {
                logger.warn(response.body());
            }
        } catch (JsonProcessingException jsonExc) {
            logger.error("Got exception trying to build API client body data: " + user.getUsername() + "\n" + jsonExc.getMessage());
        } catch (IOException | InterruptedException httpExc) {
            logger.error("Got exception from HTTP request to API client: " + httpExc.getMessage());
        }
    }
}