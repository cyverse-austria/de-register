package com.cyverse.keycloak.notification.service;

import org.keycloak.models.UserModel;

public interface NotificationService {
    void notifyUser(UserModel user);
}
