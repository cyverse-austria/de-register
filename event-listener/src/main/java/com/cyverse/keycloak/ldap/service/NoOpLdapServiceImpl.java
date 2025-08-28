package com.cyverse.keycloak.ldap.service;

import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

public class NoOpLdapServiceImpl implements LdapService {
    private static final Logger logger = Logger.getLogger(NoOpLdapServiceImpl.class);

    @Override
    public void updateLdapUser(UserModel user) {
        logger.error("No operation possible. Check other dependencies for failures.");
    }

    @Override
    public void addLdapUserToGroup(UserModel user, String group) {
        logger.error("No operation possible. Check other dependencies for failures.");
    }
}
