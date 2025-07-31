package com.cyverse.api.config;

import lombok.Data;

@Data
public class LdapServiceConfig {
    private String host;
    private String admin;
    private String password;
    private String baseDN;
    private String everyoneGroup;
    private String firstLoginPassword;
}
