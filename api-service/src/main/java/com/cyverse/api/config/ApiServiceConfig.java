package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.Data;

import java.util.Map;

@Data
public class ApiServiceConfig implements GenericConfig {
    private Integer port;
    private String apiKey;
    private IrodsServiceConfig irodsServiceConfig;
    private LdapServiceConfig ldapServiceConfig;
    private Map<String, AuthUserConfig> users;
    private static Integer DEFAULT_PORT = 7000;

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        if (port == null) {
            port = DEFAULT_PORT;
        }
        String missing = "%s missing from API Service config file.";
        if (irodsServiceConfig == null) {
            throw new ConfigException(String.format(missing, "irodsServiceConfig"));
        }
        if (ldapServiceConfig == null) {
            throw new ConfigException(String.format(missing, "ldapServiceConfig"));
        }
        if (users != null && !users.isEmpty()) {
            for (AuthUserConfig user: users.values()) {
                user.verifyFieldsAreSet();
            }
        }
        irodsServiceConfig.verifyFieldsAreSet();
        ldapServiceConfig.verifyFieldsAreSet();
    }
}
