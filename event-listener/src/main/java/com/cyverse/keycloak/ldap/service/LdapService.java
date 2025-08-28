package com.cyverse.keycloak.ldap.service;

import org.keycloak.models.UserModel;

public interface LdapService {
    void updateLdapUser(UserModel user);
    void addLdapUserToGroup(UserModel user, String group);
}
