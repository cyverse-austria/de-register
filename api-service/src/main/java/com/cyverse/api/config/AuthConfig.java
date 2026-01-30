package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class AuthConfig implements GenericConfig {
    private String apiKey;
    private String tokenIssuer;
    private Map<String, AuthUserConfig> users;

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new ConfigException("Missing api key from auth config");
        }
        if (tokenIssuer == null || tokenIssuer.isEmpty()) {
            throw new ConfigException("Missing token issuer from auth config");
        }
        if (users == null || users.isEmpty()) {
            throw new ConfigException("Missing users configuration from auth config");
        }
        for (AuthUserConfig user: users.values()) {
            user.verifyFieldsAreSet();
        }
    }

    public static AuthConfig fromEnv(String prefix) {
        Map<String, AuthUserConfig> users = new HashMap<>();

        String userMailEnv = prefix + "AUTH_USER_MAIL_";
        String userPassEnv = prefix + "AUTH_USER_PASS_";
        for (Map.Entry<String, String> envEntry: System.getenv().entrySet()) {
            if (envEntry.getKey().contains(userMailEnv)) {
                String envName = envEntry.getKey();
                String user = envName.substring(userMailEnv.length());
                if (!users.containsKey(user)) {
                    String mail = envEntry.getValue();
                    String password = System.getenv(userPassEnv + user);
                    AuthUserConfig auc = new AuthUserConfig(mail, password);
                    users.put(user, auc);
                }
            }
        }

        return new AuthConfig(
                System.getenv(prefix + "AUTH_API_KEY"),
                System.getenv(prefix+ "AUTH_TOKEN_ISSUER"),
                users
        );
    }
}
