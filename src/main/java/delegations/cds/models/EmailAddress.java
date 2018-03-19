package delegations.cds.models;


import java.io.UnsupportedEncodingException;

import javax.mail.internet.InternetAddress;

import org.immutables.value.Value;


@Value.Immutable
public abstract class EmailAddress {

    private static final String UTF8 = "UTF-8";

    public abstract String name();
    public abstract String email();

    @Value.Derived
    public InternetAddress toInternetAddress() {
        try {
            return new InternetAddress(email(), name(), UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Cannot convert to Internet Address", e);
        }
    }

}
