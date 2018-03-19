package delegations.cds.lib;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;

public class MailProducers {

    @Resource(name = "java:jboss/mail/default")
    private Session mailSession;

    @Inject
    Logger logger;
    
    @Produces
    public Consumer<Message> defaultMailTransport() {
        return message -> {
            try {
                Transport.send(message);
            } catch (MessagingException e) {
            	logger.error("Failed to send message", e);
            }
        };
    }

    @Produces
    public Supplier<Message> defaultMailSession() {
        return () -> new MimeMessage(mailSession);
    }
}
