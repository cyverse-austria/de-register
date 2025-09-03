package com.cyverse.api.services;

import com.cyverse.api.config.UserPortalServiceConfig;
import com.cyverse.api.exceptions.UserPortalException;
import com.cyverse.api.models.UserModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CyVerse User Portal service for managing users through Portal's public API.
 */
public class UserPortalService {
    private static final Logger logger = LoggerFactory.getLogger(UserPortalService.class);
    private final UserPortalServiceConfig config;
    private ApiHttpClient httpClient;
    private final Map<String, Integer> defaultProperties;

    private static final String PORTAL_USERS_ENDPOINT = "/api/users";
    private static final String PORTAL_USERS_EXISTS_ENDPOINT = "/api/exists";
    private static final String PORTAL_PROPERTIES_ENDPOINT = "/api/users/properties";
    private static final String INSTITUTIONS_EXTRA = "/institutions?keyword=other";
    private static final String USERNAME_FIELD = "username";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String PORTAL_STATUS_LOG = "PORTAL-API RESPONSE STATUS CODE: {}";

    public UserPortalService(UserPortalServiceConfig config) {
        this.config = config;
        this.defaultProperties = new HashMap<>();
    }

    public void init() {
        httpClient = new ApiHttpClient(config.getHost());
    }

    /**
     * Adds the user in the CyVerse User Portal DB.
     * For now most attributes will be default values.
     *
     * @param user the UserModel that comes from Keycloak data-model
     */
    public void addUserToPortal(UserModel user) throws UserPortalException {
        if (userExists(user.getUsername())) {
            logger.debug("User already exists, exiting ...");
            return;
        }

        setDefaultIDs();

        logger.debug("Try adding user to User Portal: {}", user.getUsername());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = buildUserPortalData(user);
        if (data == null) {
            return;
        }

        try {
            String jsonBody = mapper.writeValueAsString(data);

            HttpResponse<String> response =
                    httpClient.getHttpClient()
                            .send(httpClient
                                            .getRequestPUTnoAuth(PORTAL_USERS_ENDPOINT, jsonBody),
                                    HttpResponse.BodyHandlers.ofString());

            logger.debug(PORTAL_STATUS_LOG, response.statusCode());

            if (response.statusCode() == HttpStatus.SC_OK) {
                logger.info("Successfully added user {} in User Portal", user.getUsername());
            }
        } catch (JsonProcessingException jsonExc) {
            logger.error("Got exception trying to build API client body data: {}\n{}", user.getUsername(), jsonExc.getMessage());
        } catch (IOException | InterruptedException httpExc) {
            logger.error("Got exception from HTTP request to API client: {}", httpExc.getMessage());
        }
    }

    /**
     * Check if user already exists in the User Portal DB.
     */
    private boolean userExists(String username) {
        logger.debug("Checking if user already exists: {}", username);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = Map.of(
                "username", username
        );

        try {
            String jsonBody = mapper.writeValueAsString(data);

            HttpResponse<String> response =
                    httpClient.getHttpClient()
                            .send(httpClient
                                            .getRequestPOSTnoAuth(PORTAL_USERS_EXISTS_ENDPOINT, jsonBody),
                                    HttpResponse.BodyHandlers.ofString());

            logger.debug(PORTAL_STATUS_LOG, response.statusCode());

            Map<?, ?> parsedResponse = mapper.readValue(response.body(), Map.class);
            if (!(parsedResponse.containsKey(USERNAME_FIELD))) {
                logger.error("Got malformed response from API: {}", response.body());
                return false;
            }

            if (response.statusCode() == HttpStatus.SC_OK
                    && parsedResponse.get(USERNAME_FIELD)
                    .getClass()
                    .isAssignableFrom(String.class)) {
                return true;
            }
        } catch (JsonProcessingException jsonExc) {
            logger.error("Got exception trying to build API client body data: {}\n{}", username, jsonExc.getMessage());
        } catch (IOException | InterruptedException httpExc) {
            logger.error("Got exception from HTTP request to API client: {}", httpExc.getMessage());
        }

        return false;
    }

    private void setDefaultIDs() throws UserPortalException {
        try {
            logger.debug("Getting user properties from portal API");

            HttpResponse<String> response =
                    httpClient.getHttpClient()
                            .send(httpClient
                                            .getRequestGETnoAuth(PORTAL_PROPERTIES_ENDPOINT),
                                    HttpResponse.BodyHandlers.ofString());

            logger.debug(PORTAL_STATUS_LOG, response.statusCode());

            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<Map<?, ?>>> parsedResponse = mapper.readValue(response.body(), Map.class);

            for (String property : parsedResponse.keySet()) {
                // default value: first id value from list
                Map<?, ?> firstProperty = parsedResponse.get(property).get(0);
                defaultProperties.put(
                        getSingularPropertyIdFieldName(property),
                        (Integer) firstProperty.get("id")
                );
            }

            setDefaultInstitutionID();
        } catch (Exception e) {
            throw new UserPortalException("Could not set user default properties.\n" + e.getMessage());
        }
    }

    protected String getSingularPropertyIdFieldName(String property) {
        String singularName = property.substring(0, property.length()-1);
        if (singularName.endsWith("ie")) {
            singularName = singularName.substring(0, singularName.length()-2) + "y";
        }
        return singularName + "_id";
    }

    private void setDefaultInstitutionID() throws IOException, InterruptedException {
        logger.debug("Getting default institution from portal API");

        HttpResponse<String> response =
                httpClient.getHttpClient()
                        .send(httpClient
                                        .getRequestGETnoAuth(
                                                PORTAL_PROPERTIES_ENDPOINT
                                                        + INSTITUTIONS_EXTRA),
                                HttpResponse.BodyHandlers.ofString());

        logger.debug(PORTAL_STATUS_LOG, response.statusCode());

        ObjectMapper mapper = new ObjectMapper();
        List<Map<?,?>> parsedResponse = mapper.readValue(response.body(), List.class);
        defaultProperties.put("grid_institution_id", (Integer)parsedResponse.get(0).get("id"));
    }

    private Map<String, Object> buildUserPortalData(UserModel user) {
        Map<String, Object> data = new HashMap<>();

        data.put("username", user.getUsername());
        data.put(String.valueOf(config.getDivisor() + 1), user.getFirstName());
        data.put(String.valueOf(config.getDivisor() + 2), user.getLastName());
        data.put("email", user.getEmail());
        data.put("department", "Other");
        data.putAll(defaultProperties);

        Date now = new Date();

        int minTimeOnPage = 31 * 1000;
        try {
            data.put("plt", generateHMAC(String.valueOf(now.getTime() - minTimeOnPage)));
        } catch (Exception e) {
            logger.error("HMAC Error: {}", e.getMessage());
            return null;
        }

        return data;
    }

    // --------- region HMAC ----------------
    private String generateHMAC(String data) throws Exception {
        byte[] key = getKey();
        byte[] iv = getIV(key);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encrypted);
    }

    private byte[] getKey() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(config.getHmacKey().getBytes(StandardCharsets.UTF_8));

        return digest.digest();
    }

    private static byte[] getIV(byte[] key) {
        byte[] iv = new byte[16];
        System.arraycopy(key, 0, iv, 0, 16);
        return iv;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
