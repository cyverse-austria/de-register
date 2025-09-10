package com.cyverse.api.services;

import com.cyverse.api.config.MailServiceConfig;
import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Mail service based on jakarta.mail.
 */
public class MailServiceImpl implements MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);
    private MailServiceConfig config;
    private Set<String> mailsAlreadySent;

    public MailServiceImpl(MailServiceConfig config) {
        this.config = config;
        this.mailsAlreadySent = new HashSet<>();
    }

    /**
     *  Send the password in an email to the user creating the account, using jakarta.mail
     *  library functionalities.
     */
    @Override
    public void sendEmail(String emailTo, String password)
            throws MessagingException, ResourceAlreadyExistsException {
        if (mailsAlreadySent.contains(emailTo)) {
            throw new ResourceAlreadyExistsException("Notification was already sent to this email");
        }

        logger.debug("Sending mail to {} with LDAP/iRODS", emailTo);

        String from = config.getFromSender();
        String subject = "CyVerse LDAP/iRODS password";
        String body = "A new LDAP and iRODS password was generated for this user: <b>" + password + "</b>";

        Properties props = new Properties();
        props.put("mail.smtp.host", config.getHost());
        props.put("mail.smtp.port", config.getPort());
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");

        Session session = Session.getInstance(props);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(emailTo)
        );
        message.setSubject(subject);
        message.setContent(body, "text/html");

        Transport.send(message);

        this.mailsAlreadySent.add(emailTo);
        logger.debug("Mail sent successfully to {}", emailTo);
    }
}
