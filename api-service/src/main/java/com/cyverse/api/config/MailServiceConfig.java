package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.Data;

@Data
public class MailServiceConfig implements GenericConfig {
    private String host;

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        if (host == null || host.isEmpty()) {
            throw new ConfigException("Missing mail service host from mailServiceConfig");
        }
    }
}
