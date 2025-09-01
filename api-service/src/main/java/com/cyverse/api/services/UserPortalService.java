package com.cyverse.api.services;

import com.cyverse.api.config.UserPortalServiceConfig;
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
import java.util.Map;

/**
 * CyVerse User Portal service for managing users through Portal's public API.
 */
public class UserPortalService {
    private static final Logger logger = LoggerFactory.getLogger(UserPortalService.class);
    private final UserPortalServiceConfig config;
    private ApiHttpClient httpClient;

    private static final String PORTAL_USERS_ENDPOINT = "/api/users";
    private static final String PORTAL_USERS_EXISTS_ENDPOINT = "/api/exists";
    private static final Integer DEFAULT_USER_INFO_ID = 1;
    private static final String USERNAME_FIELD = "username";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public UserPortalService(UserPortalServiceConfig config) {
        this.config = config;
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
    public void addUserToPortal(UserModel user) {
        if (userExists(user.getUsername())) {
            logger.debug("User already exists, exiting ...");
            return;
        }

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

            logger.debug("PORTAL-API RESPONSE STATUS CODE: {}", response.statusCode());

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

            logger.debug("PORTAL-API RESPONSE STATUS CODE: {}", response.statusCode());

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

    private Map<String, Object> buildUserPortalData(UserModel user) {
        Map<String, Object> data = new HashMap<>();

        data.put("username", user.getUsername());
        data.put(String.valueOf(config.getDivisor() + 1), user.getFirstName());
        data.put(String.valueOf(config.getDivisor() + 2), user.getLastName());
        data.put("email", user.getEmail());
        data.put("grid_institution_id", DEFAULT_USER_INFO_ID);
        data.put("department", "Other");
        data.put("occupation_id", DEFAULT_USER_INFO_ID.toString());
        data.put("research_area_id", DEFAULT_USER_INFO_ID.toString());
        data.put("funding_agency_id", DEFAULT_USER_INFO_ID.toString());
        data.put("country_id", DEFAULT_USER_INFO_ID);
        data.put("region_id", DEFAULT_USER_INFO_ID.toString());
        data.put("gender_id", DEFAULT_USER_INFO_ID.toString());
        data.put("ethnicity_id", DEFAULT_USER_INFO_ID.toString());
        data.put("aware_channel_id", DEFAULT_USER_INFO_ID.toString());

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
