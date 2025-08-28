package com.cyverse.keycloak.irods.service;

import org.keycloak.models.UserModel;

public interface IrodsService {
    void addIrodsUser(UserModel user);
    void grantIrodsUserAccess(UserModel user);
}
