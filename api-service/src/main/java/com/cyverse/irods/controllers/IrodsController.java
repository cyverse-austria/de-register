package com.cyverse.irods.controllers;

import com.cyverse.irods.services.IrodsService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.io.IOException;
import java.util.Map;

public class IrodsController {

    public class IrodsException extends Exception {
        public IrodsException(String msg) {
            super(msg);
        }
    }

    private IrodsService irodsService;

    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]{3,20}$";

    public IrodsController(IrodsService irodsService) {
        this.irodsService = irodsService;
    }

    public void addIrodsUser(Context ctx)
            throws IOException, InterruptedException, IrodsException {
        Map<?, ?> data = ctx.bodyAsClass(Map.class);
        String username = (String) data.get("username");
        validateUsername(username);
        irodsService.addIrodsUser(username);
        ctx.status(HttpStatus.CREATED);
    }

    private void validateUsername(String username) throws IrodsException {
        if (username == null || username.isEmpty() || !username.matches(USERNAME_REGEX)) {
            throw new IrodsException("Username not permitted");
        }
    }
}
