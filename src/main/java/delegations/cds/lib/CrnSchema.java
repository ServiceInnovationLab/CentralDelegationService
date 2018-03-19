package delegations.cds.lib;

import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public class CrnSchema {

    private static final String PROTOCOL = "crn";

    private static final Integer CLIENT = 1;
    private static final Integer DELEGATION_TYPE = 2;
    private static final Integer RESOURCE = 2;

    private static final String REGEX = "crn:[a-z]+:[a-z]+:([0-9]{12})?:[a-zA-Z0-9_.-]+(\\/[a-zA-Z0-9_.-]+)*";
    private static final String TWELVE_DIGITS = "[0-9]{12}";

    private CrnSchema() {
    	// utility class
    }
    /*
     * Crns are in the form "protocol:privacy_domain:service:account:resource_path"
     *
     * resource_paths may contain forward slashes account may be omitted, but the SEPARATOR : must still be present, leading to a double :: eg. protocol:privacy_domain:service::resource_path
     *
     * account is always a 12 digit identifier, which means nothing to the CDS.
     *
     * resource_path is required
     *
     * Currently CDS uses CRNs for - delegation_types - resources - client
     */

    private static ValidationResponse validate(final String crn, final Integer form) {

        List<String> parts = Splitter.on(':').splitToList(crn);

        ValidationResponse vr = new ValidationResponse();

        if (parts.size() != 5) {
            vr.addMessage("Crn must have 5 parts. " + crn + " has " + parts.size());
            return vr;
        }

        String protocol = parts.get(0);
        if (Strings.isNullOrEmpty(protocol) || !protocol.equals(PROTOCOL)) {
            vr.addMessage("Crn must start with crn:");
            return vr;
        }

        if (!crn.matches(REGEX)) {
            vr.addMessage("Crn " + crn + " is not in a valid form");
        }

        String privacyDomain = parts.get(1);
        if (Strings.isNullOrEmpty(privacyDomain)) {
            vr.addMessage("Crn must contain a privacy domain");
        }

        String service = parts.get(2);
        if (Strings.isNullOrEmpty(service)) {
            vr.addMessage("Crn must contain a service");
        }

        String account = parts.get(3);
        
        if (!Strings.isNullOrEmpty(account) && !account.matches(TWELVE_DIGITS)) {
           vr.addMessage("Account numbers must be 12 digits");
        }

        // Break after Privacy Domain and Service checks
        if (!vr.isValid()) {
            return vr;
        }

        String resourcePath = parts.get(4);
        if (Strings.isNullOrEmpty(resourcePath) && form == RESOURCE) {
            vr.addMessage("Resource Crns must contain a resource path");
        }
        return vr;
    }

    public static ValidationResponse validateClient(final String crn) {
        return validate(crn, CLIENT);
    }

    public static ValidationResponse validateResource(final String crn) {
        return validate(crn, RESOURCE);
    }

    public static ValidationResponse validateDelegationType(final String crn) {
        return validate(crn, DELEGATION_TYPE);
    }

}
