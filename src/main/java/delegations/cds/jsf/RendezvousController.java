package delegations.cds.jsf;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import delegations.cds.models.Delegation;
import delegations.cds.models.DelegationType;
import delegations.cds.models.Rendezvous;
import delegations.cds.models.Resource;
import delegations.cds.services.DelegationService;
import delegations.cds.services.DelegationState;
import delegations.cds.services.RendezvousService;
import delegations.cds.services.ServiceResponse;
import delegations.cds.util.Setting;
import delegations.cds.util.Settings;

@Named
public class RendezvousController {

    @Inject
    private Logger logger;

    @Inject
    private RendezvousBean rendezvousBean;

    @Inject
    private RendezvousService rendezvousService;

    @Inject
    private DelegationService delegationService;

    public String confirm() {
        logger.info("RendezvousController:confirm for accessCode {}", rendezvousBean.getInviteCode());

        String accessCode = rendezvousBean.getInviteCode();
        String rcmsToken = Settings.getSetting(Setting.JSF_RCMS_TOKEN_DEFAULT);

        ServiceResponse<DelegationState> sr = rendezvousService.accept(accessCode, rcmsToken);

        if (sr.successful()) {
            return "finish?faces-redirect=true";
        } else {
            return "error?faces-redirect=true";
        }

    }

    public void loadParams() {
        String accessCode = rendezvousBean.getInviteCode();
        Delegation delegation = delegationService.findByInviteCode(accessCode);
        Rendezvous rendezvous = delegation.getRendezvous();
        DelegationType delegationType = delegation.getDelegationType();
        Resource resource = delegation.getResource();

        rendezvousBean.setResourceCrn(resource.getCrn());
        rendezvousBean.setDelegationTypeName(delegationType.getName());
        rendezvousBean.setDelegateEmail(rendezvous.getDelegateEmail());
        rendezvousBean.setOwnerEmail(rendezvous.getOwnerEmail());

    }
}
