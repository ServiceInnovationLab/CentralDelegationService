package delegations.cds.api;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;

@ApiModel
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DelegationRequest {

    private String resourceCrn;
    private String delegationTypeCrn;

    private String ownerRcms;
    private String ownerEmail;
    private Boolean ownerConsent = false;

    private String agentRcms;
    private String agentEmail;
    private Boolean agentConsent = false;

    public String getResourceCrn() {
        return resourceCrn;
    }

    public void setResourceCrn(final String resourceCrn) {
        this.resourceCrn = resourceCrn;
    }

    public String getDelegationTypeCrn() {
        return delegationTypeCrn;
    }

    public void setDelegationTypeCrn(final String delegationTypeCrn) {
        this.delegationTypeCrn = delegationTypeCrn;
    }

    public String getOwnerRcms() {
        return ownerRcms;
    }

    public void setOwnerRcms(final String ownerRcms) {
        this.ownerRcms = ownerRcms;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(final String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public Boolean getOwnerConsent() {
        return ownerConsent;
    }

    public void setOwnerConsent(final Boolean ownerConsent) {
        this.ownerConsent = ownerConsent;
    }

    public String getAgentRcms() {
        return agentRcms;
    }

    public void setAgentRcms(final String agentRcms) {
        this.agentRcms = agentRcms;
    }

    public String getAgentEmail() {
        return agentEmail;
    }

    public void setAgentEmail(final String agentEmail) {
        this.agentEmail = agentEmail;
    }

    public Boolean getAgentConsent() {
        return agentConsent;
    }

    public void setAgentConsent(final Boolean agentConsent) {
        this.agentConsent = agentConsent;
    }
}
