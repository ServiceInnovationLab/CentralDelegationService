package delegations.cds.lib;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import delegations.cds.models.Email;
import delegations.cds.models.EmailAddress;
import delegations.cds.models.ImmutableEmail;
import delegations.cds.models.ImmutableEmailAddress;
import delegations.cds.services.ServiceResponse;

@RunWith(JUnit4.class)
public class MailClientTest {

    private boolean transportCalled = false;
    private MailClient mailClient;

    private Logger logger = LoggerFactory.getLogger(MailClientTest.class);

    @Before
    public void setup() {
        // Inspired by https://stackoverflow.com/a/29321305/709636
        // Uses Java8 Consumer/Supplier so as not to need mocking to test Email.
        Consumer<Message> transport = message -> {
            this.transportCalled = true;
        };
        Supplier<Message> session = () -> {
            Properties properties = new Properties();
            return new MimeMessage(Session.getDefaultInstance(properties));
        };

        mailClient = new MailClient(transport, session, logger);
    }

    @Test
    public void sendBasicEmail() throws MessagingException {

        EmailAddress from = ImmutableEmailAddress.builder().email("sam@example.com").name("Sam").build();
        EmailAddress recipient = ImmutableEmailAddress.builder().email("ryan@example.com").name("Ryan").build();
        List<EmailAddress> recipients = Lists.newArrayList(recipient);

        Email testEmail = ImmutableEmail.builder()
                .fromAddress(from)
                .recipientAddresses(recipients)
                .subject("Test Subject")
                .content("Test Content")
                .build();

        ServiceResponse<Email> sr = mailClient.send(testEmail);

        assertThat(transportCalled).isTrue();
        assertThat(sr.successful()).isTrue();
    }
}
