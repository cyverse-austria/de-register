package com.cyverse.keycloak.irods.config;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IrodsConfig {
    private String irodsHost;

    public void verifyFieldsSet() {
        if (Strings.isNullOrEmpty(irodsHost)) {
            throw new IllegalStateException(
                    "iRODS Config is not complete. Please set all necessary fields.");
        }
    }
}
