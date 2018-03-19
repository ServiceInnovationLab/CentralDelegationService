package delegations.cds.auth;

import delegations.cds.models.Client;

public class AuthenticatedContext {

    private final Client authenticatedClient;

    public AuthenticatedContext(final Client client) {
        this.authenticatedClient = client;
    }

    public Client getClient() {
        return authenticatedClient;
    }

}
