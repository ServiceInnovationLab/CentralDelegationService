package delegations.cds.lib;

import delegations.cds.models.Email;
import delegations.cds.models.EmailAddress;
import delegations.cds.services.ServiceResponse;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;



@Default
public class MailClient {

    private static final String TEXT_HTML_UTF8 = "text/html; charset=utf-8";
    private static final String RELATED = "related";


    private final Logger logger;
    private final Consumer<Message> messageTransport;
    private final Supplier<Message> messageSession;

    @Inject
    public MailClient(final Consumer<Message> messageTransport, final Supplier<Message> messageSession, final Logger logger) {
        this.logger = logger;
        this.messageTransport = messageTransport;
        this.messageSession = messageSession;
    }

    public ServiceResponse<Email> send(final Email email) {

        logger.debug("Sending Email to: {} : {}", email.recipientAddresses().get(0), email.subject());


        try {

            Message message = new MimeMessage(messageSession.get().getSession());
            message.setFrom(email.fromAddress().toInternetAddress());

            message.setRecipients(Message.RecipientType.TO, email.recipientAddresses()
                    .stream()
                    .map(EmailAddress::toInternetAddress)
                    .toArray(InternetAddress[]::new));

            message.setSubject(email.subject());
            message.setSentDate(new Date());

            MimeMultipart multipart = new MimeMultipart(RELATED);
            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(email.content(), TEXT_HTML_UTF8);
            multipart.addBodyPart(bodyPart);

            message.setContent(multipart);

            messageTransport.accept(message);

            logger.debug("Email was sent");

            return ServiceResponse.forSuccess(email);

        } catch (MessagingException | IllegalStateException e) {
            logger.error("Error while sending email : {}", e);
            return ServiceResponse.forException(e);
        }

    }
}
