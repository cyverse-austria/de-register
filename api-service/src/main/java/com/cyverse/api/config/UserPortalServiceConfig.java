package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPortalServiceConfig implements GenericConfig {
    private String host;
    private String hmacKey;
    private Integer divisor;

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        if (host == null || host.isEmpty()
                || hmacKey == null || hmacKey.isEmpty()
                || divisor == null) {
            throw new ConfigException("host, hmacKey and divisor are required for user portal configuration");
        }
    }

    public static UserPortalServiceConfig fromEnv(String prefix) {
        return new UserPortalServiceConfig(
                System.getenv(prefix + "PORTAL_HOST"),
                System.getenv(prefix+ "PORTAL_HMAC_KEY"),
                Integer.valueOf(System.getenv(prefix+"PORTAL_DIVISOR"))
        );
    }
}
