package com.cyverse.api.config;

import lombok.Data;

@Data
public class ApiServiceConfig {
    private IrodsServiceConfig irodsServiceConfig;
    private LdapServiceConfig ldapServiceConfig;
}
