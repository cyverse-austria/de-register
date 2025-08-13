package com.cyverse.api.controllers;

import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
import com.cyverse.api.exceptions.UserException;
import com.cyverse.api.models.UserModel;
import com.cyverse.api.services.LdapService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;

import javax.naming.NamingException;
import java.security.NoSuchAlgorithmException;

public class LdapController {

    private LdapService ldapService;

    public LdapController(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @OpenApi(
            summary = "Create a new LDAP user account",
            operationId = "addLdapUser",
            path = "/api/users/ldap",
            methods = HttpMethod.POST,
            requestBody = @OpenApiRequestBody(
                    content = {@OpenApiContent(from = UserModel.class)}
            ),
            responses = {@OpenApiResponse(status = "201")}
    )
    public void addLdapUser(Context ctx)
            throws UserException, ResourceAlreadyExistsException,
            NamingException, NoSuchAlgorithmException {
        UserModel user = ctx.bodyAsClass(UserModel.class);
        user.validateUsername();
        ldapService.addLdapUser(user);
        ctx.status(HttpStatus.CREATED);
    }

    @OpenApi(
            summary = "Update an existing LDAP user account",
            operationId = "updateLdapUser",
            path = "/api/users/ldap",
            methods = HttpMethod.PUT,
            requestBody = @OpenApiRequestBody(
                    content = {@OpenApiContent(from = UserModel.class)}
            ),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void updateLdapUser(Context ctx)
            throws UserException, ResourceAlreadyExistsException,
            NamingException, NoSuchAlgorithmException {
        UserModel user = ctx.bodyAsClass(UserModel.class);
        user.validateUsername();
        ldapService.completeLdapUserAttributes(user);
        ctx.status(HttpStatus.OK);
    }

    @OpenApi(
            summary = "Adds an LDAP user to a specified LDAP group",
            operationId = "addLdapUserToGroup",
            path = "/api/groups/ldap",
            methods = HttpMethod.PUT,
            requestBody = @OpenApiRequestBody(
                    content = {@OpenApiContent(from = UserModel.class)}
            ),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void addLdapUserToGroup(Context ctx)
            throws UserException, ResourceAlreadyExistsException, NamingException {
        UserModel request = ctx.bodyAsClass(UserModel.class);
        request.validateUsername();
        if (request.getGroup() == null) {
            throw new UserException("Group is missing");
        }
        ldapService.addLdapUserToGroup(request.getUsername(), request.getGroup());
        ctx.status(HttpStatus.OK);
    }
}
