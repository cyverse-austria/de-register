package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.Data;

@Data
public class AuthUserConfig implements GenericConfig {
    private String mail;
    private String password;

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        if (mail == null
                || mail.isEmpty()
                || password == null
                || password.isEmpty()) {
            throw new ConfigException(
                    "Username or password is missing from user config");
        }
    }
}
