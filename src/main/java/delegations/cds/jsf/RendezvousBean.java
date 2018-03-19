package delegations.cds.jsf;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class RendezvousBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String inviteCode;

    private String rcmsToken;

    private String ownerEmail;

    private String delegateEmail;

    private String delegationTypeName;

    private String resourceCrn;

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getRcmsToken() {
        return rcmsToken;
    }

    public void setRcmsToken(String rcmsToken) {
        this.rcmsToken = rcmsToken;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getDelegateEmail() {
        return delegateEmail;
    }

    public void setDelegateEmail(String delegateEmail) {
        this.delegateEmail = delegateEmail;
    }

    public String getDelegationTypeName() {
        return delegationTypeName;
    }

    public void setDelegationTypeName(String delegationTypeName) {
        this.delegationTypeName = delegationTypeName;
    }

    public String getResourceCrn() {
        return resourceCrn;
    }

    public void setResourceCrn(String resourceCrn) {
        this.resourceCrn = resourceCrn;
    }
}
