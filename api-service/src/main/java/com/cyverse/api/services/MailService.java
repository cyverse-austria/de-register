package com.cyverse.api.services;

import jakarta.mail.MessagingException;

public interface MailService {
    void sendEmail(String email, String password) throws MessagingException;
}
