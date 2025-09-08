package com.cyverse.api.controllers;

import com.cyverse.api.exceptions.UserException;
import com.cyverse.api.models.UserModel;
import com.cyverse.api.services.MailService;
import com.cyverse.api.services.PasswordService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;
import jakarta.mail.MessagingException;

public class MailController {
    private MailService mailService;
    private PasswordService passwordService;

    public MailController(MailService mailService, PasswordService passwordService) {
        this.mailService = mailService;
        this.passwordService = passwordService;
    }

    @OpenApi(
        summary = "Send a notification with the password for a given user",
        operationId = "sendPasswordNotification",
        path = "/api/users/notification",
        methods = HttpMethod.PUT,
        security = @OpenApiSecurity(name = "Bearer"),
        requestBody = @OpenApiRequestBody(
                content = {@OpenApiContent(from = UserModel.class)}
        ),
        responses = {@OpenApiResponse(status = "200")}
    )
    public void sendPasswordNotification(Context ctx)
            throws UserException, MessagingException {
        UserModel user = ctx.bodyAsClass(UserModel.class);
        validateUser(user);
        String password = passwordService.getPassword(user.getUsername());
        if (password == null) {
            throw new UserException("Password for this user is still not set.");
        }
        mailService.sendEmail(user.getEmail(), password);
        ctx.status(HttpStatus.OK);
    }

    private void validateUser(UserModel user) throws UserException {
        user.validateUsername();
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new UserException("Mail not provided");
        }
    }
}
