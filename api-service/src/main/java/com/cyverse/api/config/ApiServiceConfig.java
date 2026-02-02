package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiServiceConfig implements GenericConfig {
    private Integer port;
    private IrodsServiceConfig irodsServiceConfig;
    private LdapServiceConfig ldapServiceConfig;
    private AuthConfig authConfig;
    private UserPortalServiceConfig userPortalServiceConfig;

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
        if (userPortalServiceConfig == null) {
            throw new ConfigException(String.format(missing, "userPortalServiceConfig"));
        }
        if (authConfig != null) {
            authConfig.verifyFieldsAreSet();
        }
        irodsServiceConfig.verifyFieldsAreSet();
        ldapServiceConfig.verifyFieldsAreSet();
        userPortalServiceConfig.verifyFieldsAreSet();
    }

    public static ApiServiceConfig fromEnv(EnvHelper envHelper, String prefix) {
        return new ApiServiceConfig(
                Integer.valueOf(envHelper.getEnv(prefix + "PORT")),
                IrodsServiceConfig.fromEnv(envHelper, prefix),
                LdapServiceConfig.fromEnv(envHelper, prefix),
                AuthConfig.fromEnv(envHelper, prefix),
                UserPortalServiceConfig.fromEnv(envHelper, prefix)
        );
    }
}
