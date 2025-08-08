package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;

/**
 * Base config interface for all api-service configs.
 */
public interface GenericConfig {
    void verifyFieldsAreSet() throws ConfigException;
}
