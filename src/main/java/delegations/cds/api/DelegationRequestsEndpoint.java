package delegations.cds.api;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import delegations.cds.services.DelegationRequestService;
import delegations.cds.services.DelegationState;
import delegations.cds.services.ServiceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

/**
 * The DelegationRequestsEndpoint creates a delegation.
 *
 */
@Api(authorizations = @Authorization("client-auth"))
@Path("/delegation_requests")
public class DelegationRequestsEndpoint extends ApiEndpoint {

    @Inject
    private DelegationRequestService delegationRequestService;

    /**
     * Create a delegation.
     * 
     */
    @ApiOperation(
            value = "Creates a delegation request",
            tags = {"core", "delegation_requests"})
    @POST
    public Response create(@Context final UriInfo ui, final DelegationRequest params) {
        logger.info("delegation_requests::create");
        if (params == null) {
            return Response.status(BAD_REQUEST).entity("Request cannot be null").build();
        }

        ServiceResponse<DelegationState> sr = delegationRequestService.create(params);

        // for completed delegation, return access uri for the protected resource
        // for rendezvous pending, return the completion uri for the rendezvous
        // plus metadata about the state of things, obvs

        if (sr.successful()) {
            DelegationStatusView response = DelegationStatusView.from(ui.getBaseUriBuilder().toTemplate(), sr.asSuccess().payload());

            if (response.isDelegationReady()) {
                return Response.ok(response).build();
            } else {
                return Response.accepted(response).build();
            }

        } else {
            return Response.status(BAD_REQUEST).entity(sr.asError().message()).build();
        }
    }

}
