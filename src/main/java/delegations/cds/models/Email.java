package delegations.cds.models;

import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

@Value.Immutable
public interface Email {

    public abstract String subject();

    public abstract String content();

    public abstract EmailAddress fromAddress();

    @Nullable
    public abstract EmailAddress senderAddress();

    @Nullable
    public abstract EmailAddress replyToAddress();

    public abstract List<EmailAddress> recipientAddresses();

    public abstract List<EmailAddress> copyAddresses();

    public abstract List<File> attachments();

}
