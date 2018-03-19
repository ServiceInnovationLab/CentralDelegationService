package delegations.cds.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import delegations.cds.services.DelegationState;
import javax.ws.rs.core.UriBuilder;

@JsonInclude(Include.NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
public class DelegationStatusView {
    private boolean delegationReady;
    private String accessResourceUri;
    private String completeOwnerRendezvousUri;
    private String completeOwnerRendezvousCode;
    private String completeDelegateRendezvousUri;
    private String completeDelegateRendezvousCode;

    public static DelegationStatusView from(final String basePath, final DelegationState result) {
        DelegationStatusView response = new DelegationStatusView();
        response.delegationReady = result.getProtectedResource() != null;

        // note: UriBuilder is not immutable; builder methods will alter the underlying instance.
        // This means we need a new UriBuilder for each uri we build.

        if (response.delegationReady) {
            response.accessResourceUri = UriBuilder.fromUri(basePath).path("resources/{crn}/access/{rcms_token}")
                    .resolveTemplate("crn", result.getProtectedResource().getCrn(), false)
                    .toTemplate();
        } else {
            if (result.getPendingRendezvous().getOwnerCode() != null && !result.getPendingRendezvous().isOwnerCodeConsumed()) {
                response.completeOwnerRendezvousCode = result.getPendingRendezvous().getOwnerCode();
                response.completeOwnerRendezvousUri = UriBuilder.fromUri(basePath).path("rendezvous/{code}/accept/{rcms_token}")
                        .resolveTemplate("code", response.completeOwnerRendezvousCode)
                        .toTemplate();
            }

            if (result.getPendingRendezvous().getDelegateCode() != null && !result.getPendingRendezvous().isDelegateCodeConsumed()) {
                response.completeDelegateRendezvousCode = result.getPendingRendezvous().getDelegateCode();
                response.completeDelegateRendezvousUri = UriBuilder.fromUri(basePath).path("rendezvous/{code}/accept/{rcms_token}")
                        .resolveTemplate("code", response.completeDelegateRendezvousCode)
                        .toTemplate();
            }
        }
        return response;
    }

    public boolean isDelegationReady() {
        return delegationReady;
    }

    public void setDelegationReady(final boolean delegationReady) {
        this.delegationReady = delegationReady;
    }

    public String getAccessResourceUri() {
        return accessResourceUri;
    }

    public void setAccessResourceUri(final String accessResourceUri) {
        this.accessResourceUri = accessResourceUri;
    }

    public String getCompleteOwnerRendezvousUri() {
        return completeOwnerRendezvousUri;
    }

    public void setCompleteOwnerRendezvousUri(final String completeOwnerRendezvousUri) {
        this.completeOwnerRendezvousUri = completeOwnerRendezvousUri;
    }

    public String getCompleteDelegateRendezvousUri() {
        return completeDelegateRendezvousUri;
    }

    public void setCompleteDelegateRendezvousUri(final String completeDelegateRendezvousUri) {
        this.completeDelegateRendezvousUri = completeDelegateRendezvousUri;
    }

    public String getCompleteOwnerRendezvousCode() {
        return completeOwnerRendezvousCode;
    }

    public void setCompleteOwnerRendezvousCode(final String completeOwnerRendezvousCode) {
        this.completeOwnerRendezvousCode = completeOwnerRendezvousCode;
    }

    public String getCompleteDelegateRendezvousCode() {
        return completeDelegateRendezvousCode;
    }

    public void setCompleteDelegateRendezvousCode(final String completeDelegateRendezvousCode) {
        this.completeDelegateRendezvousCode = completeDelegateRendezvousCode;
    }

}
