package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.Data;

@Data
public class ApiServiceConfig implements GenericConfig {
    private IrodsServiceConfig irodsServiceConfig;
    private LdapServiceConfig ldapServiceConfig;

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        String missing = "%s missing from API Service config file.";
        if (irodsServiceConfig == null) {
            throw new ConfigException(String.format(missing, "irodsServiceConfig"));
        }
        if (ldapServiceConfig == null) {
            throw new ConfigException(String.format(missing, "ldapServiceConfig"));
        }
        irodsServiceConfig.verifyFieldsAreSet();
        ldapServiceConfig.verifyFieldsAreSet();
    }
}
