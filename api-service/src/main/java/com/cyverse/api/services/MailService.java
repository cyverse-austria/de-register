package com.cyverse.api.services;

import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
import jakarta.mail.MessagingException;

public interface MailService {
    void sendEmail(String email, String password) throws MessagingException, ResourceAlreadyExistsException;
}
