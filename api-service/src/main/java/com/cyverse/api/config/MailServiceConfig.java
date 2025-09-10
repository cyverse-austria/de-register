package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.Data;

@Data
public class MailServiceConfig implements GenericConfig {
    private String host;
    private Integer port;
    private String fromSender;

    private static final Integer DEFAULT_PORT = 25;
    private static final String DEFAULT_FROM_SENDER = "api-service@example.com";

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        if (host == null || host.isEmpty()) {
            throw new ConfigException("Missing mail service host from mailServiceConfig");
        }
        if (port == null) {
            port = DEFAULT_PORT;
        }
        if (fromSender == null || fromSender.isEmpty()) {
            fromSender = DEFAULT_FROM_SENDER;
        }
    }
}
