package com.cyverse.keycloak.irods.service;

import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

public class NoOpIrodsServiceImpl implements IrodsService {
    private static final Logger logger = Logger.getLogger(NoOpIrodsServiceImpl.class);

    @Override
    public void addIrodsUser(UserModel user) {
        logger.error("No operation possible. Check other dependencies for failures.");
    }

    @Override
    public void grantIrodsUserAccess(UserModel user) {
        logger.error("No operation possible. Check other dependencies for failures.");
    }
}
