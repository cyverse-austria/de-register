package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.Data;

import java.util.Map;

@Data
public class AuthConfig implements GenericConfig {
    private String apiKey;
    private Map<String, AuthUserConfig> users;

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new ConfigException("Missing api key from auth config");
        }
        if (users == null || users.isEmpty()) {
            throw new ConfigException("Missing users configuration from auth config");
        }
        for (AuthUserConfig user: users.values()) {
            user.verifyFieldsAreSet();
        }
    }
}
