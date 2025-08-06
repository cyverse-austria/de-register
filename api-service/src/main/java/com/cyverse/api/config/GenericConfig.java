package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;

public interface GenericConfig {
    void verifyFieldsAreSet() throws ConfigException;
}
