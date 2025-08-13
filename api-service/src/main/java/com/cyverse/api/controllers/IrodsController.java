package com.cyverse.api.controllers;

import com.cyverse.api.exceptions.IrodsException;
import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
import com.cyverse.api.exceptions.UserException;
import com.cyverse.api.models.UserModel;
import com.cyverse.api.services.IrodsService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;

import java.io.IOException;
import java.util.Map;

public class IrodsController {

    private IrodsService irodsService;

    public IrodsController(IrodsService irodsService) {
        this.irodsService = irodsService;
    }

    @OpenApi(
            summary = "Create a new iRODS user account",
            operationId = "addIrodsUser",
            path = "/api/users/irods",
            methods = HttpMethod.POST,
            requestBody = @OpenApiRequestBody(
                    content = {@OpenApiContent(from = UserModel.class)}
            ),
            responses = {@OpenApiResponse(status = "201")}
    )
    public void addIrodsUser(Context ctx)
            throws IOException, InterruptedException,
            UserException, IrodsException, ResourceAlreadyExistsException {
        UserModel user = ctx.bodyAsClass(UserModel.class);
        user.validateUsername();
        irodsService.addIrodsUser(user.getUsername());
        ctx.status(HttpStatus.CREATED);
    }

    @OpenApi(
            summary = "Grant iRODS admin group access to a user's directory",
            description = "Specific administration groups need access to " +
                    "newly created irods users directories. This method provides that",
            operationId = "grantUserAccess",
            path = "/api/users/irods",
            methods = HttpMethod.PUT,
            requestBody = @OpenApiRequestBody(
                    content = {@OpenApiContent(from = UserModel.class)}
            ),
            responses = {@OpenApiResponse(status = "200")}
    )
    public void grantUserAccess(Context ctx)
            throws IOException, InterruptedException,
            UserException, IrodsException, ResourceAlreadyExistsException {
        UserModel user = ctx.bodyAsClass(UserModel.class);
        user.validateUsername();
        irodsService.grantAccessToUser(user.getUsername());
        ctx.status(HttpStatus.OK);
    }
}
