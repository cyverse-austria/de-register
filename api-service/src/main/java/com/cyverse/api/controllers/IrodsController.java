package com.cyverse.api.controllers;

import com.cyverse.api.exceptions.UserException;
import com.cyverse.api.models.UserModel;
import com.cyverse.api.services.IrodsService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.io.IOException;
import java.util.Map;

public class IrodsController {

    private IrodsService irodsService;

    public IrodsController(IrodsService irodsService) {
        this.irodsService = irodsService;
    }

    public void addIrodsUser(Context ctx)
            throws IOException, InterruptedException, UserException {
        UserModel user = ctx.bodyAsClass(UserModel.class);
        user.validateUsername();
        irodsService.addIrodsUser(user.getUsername());
        ctx.status(HttpStatus.CREATED);
    }
}
