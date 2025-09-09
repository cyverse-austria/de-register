package com.cyverse.keycloak.notification.service;

import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;

public class NoOpNotificationServiceImpl implements NotificationService {
    private static final Logger logger = Logger.getLogger(NoOpNotificationServiceImpl.class);

    @Override
    public void notifyUser(UserModel user) {
        logger.error("No operation possible. Check other dependencies for failures.");
    }
}
