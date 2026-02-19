package com.cyverse.keycloak.ldap.service;

import org.keycloak.models.UserModel;

import java.util.Map;

public interface LdapService {
    Map<String, String> updateLdapUser(UserModel user);
    void addLdapUserToGroup(UserModel user, String group);
}
