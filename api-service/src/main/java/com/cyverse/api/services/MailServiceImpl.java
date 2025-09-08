package com.cyverse.api.services;

import com.cyverse.api.config.MailServiceConfig;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class MailServiceImpl implements MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);
    private MailServiceConfig config;

    public MailServiceImpl(MailServiceConfig config) {
        this.config = config;
    }

    /**
     *  Send the password in an email to the user creating the account, using jakarta.mail
     *  library functionalities.
     */
    @Override
    public void sendEmail(String email, String password) throws MessagingException {
        String from = "sender@example.com";
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test body";

        Properties props = new Properties();
        props.put("mail.smtp.host", config.getHost());
        props.put("mail.smtp.port", 25);
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        Session session = Session.getInstance(props);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to)
        );
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
