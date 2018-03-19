package delegations.cds.api;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import delegations.cds.models.Delegation;
import delegations.cds.services.DelegationService;
import delegations.cds.views.DelegationView;
import delegations.cds.views.DelegationsView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ResponseHeader;

/**
 * The DelegationsEndpoint lists delegations.
 * Either a view of all delegations is returned, or a view for a particular one. 
 *
 */
@Api(authorizations = @Authorization("client-auth"))
@Path("/delegations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DelegationsEndpoint {

    @Inject
    private DelegationService delegationService;

    /**
     * List all delegations.
     * 
     */
    @ApiOperation(
            value = "Finds delegations",
            code = 200,
            response = Delegation.class,
            tags = {"info", "delegations"})
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Success",
                    response = Delegation.class,
                    responseContainer = "List"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized",
                    responseHeaders = @ResponseHeader(
                            name = HttpHeaders.WWW_AUTHENTICATE,
                            description = "Challenge for Basic Auth",
                            response = String.class))
    })
    @GET
    public Response doGet() {
        DelegationsView view = delegationService.findAll();

        return Response.ok().entity(view).build();
    }

    /**
     * List delegation for a given crn.
     * 
     */
    @ApiOperation(
            value = "Gets a delegation",
            tags = {"info", "delegations"})
    @GET
    @Path("{crn : .+}")
    public Response show(@PathParam("crn") final String crn) {

        if (crn == null) {
            throw new BadRequestException("crn must not be empty");
        }

        try {
            DelegationView view = delegationService.findByCrn(crn);
            return Response.ok().entity(view).build();
        } catch (NoResultException e) {
            throw new NotFoundException(e);
        }

    }
}
