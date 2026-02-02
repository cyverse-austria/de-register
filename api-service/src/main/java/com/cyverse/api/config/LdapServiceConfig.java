package com.cyverse.api.config;

import com.cyverse.api.exceptions.ConfigException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LdapServiceConfig implements GenericConfig {
    private String host;
    private String admin;
    private String password;
    private String baseDN;
    private String everyoneGroup;

    @Override
    public void verifyFieldsAreSet() throws ConfigException {
        String attrs = Arrays.stream(getClass().getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.joining(","));
        if (host == null || host.isEmpty()
                || admin == null || admin.isEmpty()
                || password == null || password.isEmpty()
                || baseDN == null || baseDN.isEmpty()
                || everyoneGroup == null || everyoneGroup.isEmpty()) {
            throw new ConfigException("Field missing from LDAP Service config file." +
                    "Needed attributes: " + attrs);
        }
    }

    public static LdapServiceConfig fromEnv(EnvHelper envHelper, String prefix) {
        return new LdapServiceConfig(
                envHelper.getEnv(prefix + "LDAP_HOST"),
                envHelper.getEnv(prefix + "LDAP_ADMIN"),
                envHelper.getEnv(prefix + "LDAP_PASSWORD"),
                envHelper.getEnv(prefix + "LDAP_BASE_DN"),
                envHelper.getEnv(prefix + "LDAP_EVERYONE_GROUP")
        );
    }
}
