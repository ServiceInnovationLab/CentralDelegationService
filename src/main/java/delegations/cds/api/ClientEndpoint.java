package delegations.cds.api;

import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import delegations.cds.models.Client;
import delegations.cds.services.ClientService;
import delegations.cds.services.ServiceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(authorizations = @Authorization("client-auth"))
@Path("/clients")
public class ClientEndpoint extends ApiEndpoint {

    @Inject
    private ClientService clientService;

    @ApiOperation(
            value = "Creates a client",
            tags = {"admin", "clients"})
    @POST
    public Response create(@Context final UriInfo ui, final Client client) {

        ServiceResponse<Client> sr = clientService.create(client);
        if (sr.successful()) {
            URI uri = ui.getRequestUriBuilder().path("{crn}").build(sr.asSuccess().getId());
            return Response.created(uri).build();
        } else {
            return Response.status(Status.UNAUTHORIZED).entity(sr.asError().message()).build();
        }

    }


}
