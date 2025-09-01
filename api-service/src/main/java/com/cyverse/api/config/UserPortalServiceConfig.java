package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.Data;

@Data
public class UserPortalServiceConfig implements GenericConfig {
    private String host;
    private String hmacKey;
    private Integer divisor;

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        if (host == null || host.isEmpty()
                || hmacKey == null || hmacKey.isEmpty()
                || divisor == null) {
            throw new ConfigException("host, hmacKey and divisor required for user portal configuration");
        }
    }
}
