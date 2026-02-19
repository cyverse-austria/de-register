package com.cyverse.keycloak.ldap.service;

import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.Map;

public class NoOpLdapServiceImpl implements LdapService {
    private static final Logger logger = Logger.getLogger(NoOpLdapServiceImpl.class);

    @Override
    public Map<String, String> addLdapUser(UserModel user) {
        logger.error("No operation possible. Check other dependencies for failures.");
        return new HashMap<>();
    }

    @Override
    public void addLdapUserToGroup(UserModel user, String group) {
        logger.error("No operation possible. Check other dependencies for failures.");
    }
}
