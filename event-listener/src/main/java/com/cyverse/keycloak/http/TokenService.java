package com.cyverse.keycloak.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.Map;

public class TokenService {
    private static final Logger logger = Logger.getLogger(TokenService.class);
    private String serviceMail;
    private String servicePassword;
    private static final String TOKEN_ENDPOINT = "/api/login";
    private static final Long EXPIRATION_TIME_MS = 3600 * 1000L;

    private String currentToken;
    private Date currentExpirationTime;

    public TokenService(String serviceMail, String servicePassword) {
        this.serviceMail = serviceMail;
        this.servicePassword = servicePassword;
        this.currentExpirationTime = new Date();
    }

    /**
     * Gets JWT token either first time or if it has expired. Stores it internally for
     * further reuse.
     * @return the jwt token
     */
    public String getToken(ListenerHttpClient httpClient) {
        Date now = new Date();
        if (currentToken == null || now.after(currentExpirationTime)) {
            logger.info("Token invalid: either expired or requesting the first time!");

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = Map.of(
                    "mail", serviceMail,
                    "password", servicePassword
            );

            try {
                String jsonBody = mapper.writeValueAsString(data);

                HttpResponse<String> response =
                        httpClient.getHttpClient()
                                .send(httpClient
                                                .getRequestPOSTApiKey(TOKEN_ENDPOINT, jsonBody),
                                        HttpResponse.BodyHandlers.ofString());

                logger.debug("API RESPONSE STATUS CODE: " + response.statusCode());

                if (response.statusCode() == HttpStatus.SC_OK) {
                    logger.info("Got token successfully from API");
                }

                currentExpirationTime.setTime(now.getTime() + EXPIRATION_TIME_MS);
                currentToken = response.body();
            } catch (JsonProcessingException jsonExc) {
                logger.error("Got exception trying to build token request");
            } catch (IOException | InterruptedException httpExc) {
                logger.error("Got exception from HTTP request to API: " + httpExc.getMessage());
            }
        }

        return currentToken;
    }
}
