package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.Data;

@Data
public class IrodsServiceConfig implements GenericConfig {
    private String password;
    private String zone;
    private Boolean ipcServices;

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        String missing = "%s missing from iRODS config file.";

        if (password == null || password.isEmpty()) {
            throw new ConfigException(String.format(missing, "password"));
        }
        if (zone == null || zone.isEmpty()) {
            throw new ConfigException(String.format(missing, "zone"));
        }
        if (ipcServices == null) {
            ipcServices = false;
        }
    }
}
