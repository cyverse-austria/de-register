package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

    public static UserPortalServiceConfig fromEnv(EnvHelper envHelper, String prefix) {
        return new UserPortalServiceConfig(
                envHelper.getEnv(prefix + "PORTAL_HOST"),
                envHelper.getEnv(prefix+ "PORTAL_HMAC_KEY"),
                Integer.valueOf(envHelper.getEnv(prefix+"PORTAL_DIVISOR"))
        );
    }
}
