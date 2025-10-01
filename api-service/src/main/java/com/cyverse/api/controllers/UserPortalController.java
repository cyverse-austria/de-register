package com.cyverse.api.controllers;

import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
import com.cyverse.api.exceptions.UserException;
import com.cyverse.api.exceptions.UserPortalException;
import com.cyverse.api.models.UserModel;
import com.cyverse.api.services.UserPortalService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;

public class UserPortalController {

    private final UserPortalService userPortalService;

    public UserPortalController(UserPortalService userPortalService) {
        this.userPortalService = userPortalService;
    }

    @OpenApi(
        summary = "Create a new CyVerse user portal account",
        operationId = "addUserPortalUser",
        path = "/api/users/portal",
        methods = HttpMethod.POST,
        security = @OpenApiSecurity(name = "Bearer"),
        requestBody = @OpenApiRequestBody(
                content = {@OpenApiContent(from = UserModel.class)}
        ),
        responses = {@OpenApiResponse(status = "201")}
    )
    public void addUserPortalUser(Context ctx)
            throws UserException, UserPortalException, ResourceAlreadyExistsException {
        UserModel user = ctx.bodyAsClass(UserModel.class);
        validateUser(user);
        userPortalService.addUserToPortal(user);
        ctx.status(HttpStatus.CREATED);
    }

    private void validateUser(UserModel user) throws UserException {
        user.validateUsername();
        if (user.getFirstName() == null || user.getFirstName().isEmpty()
                || user.getLastName() == null || user.getLastName().isEmpty()
                || user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new UserException("Username, first, last name and email required for creating portal account");
        }
    }
}
