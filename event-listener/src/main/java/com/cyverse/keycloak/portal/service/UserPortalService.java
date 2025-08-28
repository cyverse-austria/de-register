package com.cyverse.keycloak.portal.service;

import org.keycloak.models.UserModel;

public interface UserPortalService {
    void addUserToPortal(UserModel user);
}
