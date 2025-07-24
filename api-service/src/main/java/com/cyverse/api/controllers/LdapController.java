package com.cyverse.api.controllers;

import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
import com.cyverse.api.exceptions.UserException;
import com.cyverse.api.models.UserModel;
import com.cyverse.api.services.LdapService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class LdapController {
    private LdapService ldapService;

    public LdapController(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    public void addLdapUser(Context ctx) throws UserException, ResourceAlreadyExistsException {
        UserModel user = ctx.bodyAsClass(UserModel.class);
        user.validateUsername();
        ldapService.addLdapUser(user);
        ctx.status(HttpStatus.CREATED);
    }
}
