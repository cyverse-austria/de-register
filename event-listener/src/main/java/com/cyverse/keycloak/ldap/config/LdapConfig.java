package com.cyverse.keycloak.ldap.config;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * LDAP Service configuration.
 */
@Data
@AllArgsConstructor
public class LdapConfig {
    private String ldapHost;
    private String ldapAdmin;
    private String ldapPassword;
    private String ldapBaseDN;
    private String everyoneGroup;

    public void verifyFieldsSet() {
        if (Strings.isNullOrEmpty(ldapHost) ||
                Strings.isNullOrEmpty(ldapAdmin) ||
                Strings.isNullOrEmpty(ldapPassword) ||
                Strings.isNullOrEmpty(ldapBaseDN) ||
                Strings.isNullOrEmpty(everyoneGroup)
        ) {
            throw new IllegalStateException(
                    "LDAP Config is not complete. Please set all fields.");
        }
    }
}
