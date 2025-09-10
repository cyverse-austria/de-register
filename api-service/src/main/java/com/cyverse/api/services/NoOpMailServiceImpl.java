package com.cyverse.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpMailServiceImpl implements MailService {
    private static final Logger logger = LoggerFactory.getLogger(NoOpMailServiceImpl.class);

    @Override
    public void sendEmail(String email, String password) {
        logger.warn("No operation for mail service because it's not configured.");
    }
}
