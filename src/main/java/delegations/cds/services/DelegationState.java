package delegations.cds.services;

import delegations.cds.models.Delegation;
import delegations.cds.models.Rendezvous;
import delegations.cds.models.Resource;

public class DelegationState {
    private Resource protectedResource;
    private Rendezvous pendingRendezvous;

    public static DelegationState from(final Delegation delegation) {
        DelegationState result = new DelegationState();
        result.protectedResource = delegation.getResource();
        return result;
    }

    public static DelegationState from(final Rendezvous rendezvous) {
        DelegationState result = new DelegationState();
        result.pendingRendezvous = rendezvous;
        return result;
    }

    public Resource getProtectedResource() {
        return protectedResource;
    }

    public void setProtectedResource(final Resource protectedResource) {
        this.protectedResource = protectedResource;
    }

    public Rendezvous getPendingRendezvous() {
        return pendingRendezvous;
    }

    public void setPendingRendezvous(final Rendezvous pendingRendezvous) {
        this.pendingRendezvous = pendingRendezvous;
    }

}
