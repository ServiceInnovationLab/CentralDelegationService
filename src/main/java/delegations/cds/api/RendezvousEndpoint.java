package delegations.cds.api;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

import delegations.cds.services.DelegationState;
import delegations.cds.services.RendezvousService;
import delegations.cds.services.ServiceResponse;
import delegations.cds.views.RendezvousesView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

/**
 * The RendezvousEndpoint can list rendezvous and complete existing ones.
 * 
 * A rendezvous is what's created when a delegation can't be completed because either the owner or the delegate are unknown.
 * When the unknown party completes a rendezvous they are added to the delegation.
 *
 */
@Api(authorizations = @Authorization("client-auth"))
@Path("/rendezvous")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RendezvousEndpoint {

    @Inject
    private Logger logger;

    @Inject
    private RendezvousService rendezvousService;

    /**
     * List all rendezvous.
     * 
     */
    @ApiOperation(
            value = "Gets rendezvous",
            tags = {"admin", "rendezvous"})
    @GET
    public Response list(@BeanParam final ListParams params) {
        if (params == null) {
            return Response.status(BAD_REQUEST).entity("Entity cannot be null").build();
        }

        RendezvousesView view = rendezvousService.findAll(params.getFilter());


        return Response.ok().entity(view).build();
    }

    /**
     * Complete rendezvous for given code.
     * 
     */
    @ApiOperation(
            value = "Accepts a rendezvous",
            tags = {"core", "rendezvous"})
    @Path("/{code}/accept/{rcms}")
    @POST
    public Response complete(@Context final UriInfo ui, @PathParam("code") final String code, @PathParam("rcms") final String rcms) {

        logger.debug("Receive 'complete rendezvous' request for code = {}, rcms = {}", code, rcms);

        ServiceResponse<DelegationState> sr = rendezvousService.accept(code, rcms);

        if (sr.successful()) {
            return Response.ok()
                    .entity(DelegationStatusView.from(ui.getBaseUriBuilder().toTemplate(), sr.asSuccess().payload()))
                    .build();
        } else {
            return Response.status(Status.BAD_REQUEST)
                    .entity(sr.asError().message())
                    .build();
        }

    }
}
