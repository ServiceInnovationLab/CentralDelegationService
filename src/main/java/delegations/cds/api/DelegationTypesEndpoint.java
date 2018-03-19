package delegations.cds.api;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import delegations.cds.models.DelegationType;
import delegations.cds.services.DelegationTypeService;
import delegations.cds.services.ServiceResponse;
import delegations.cds.views.DelegationTypeView;
import delegations.cds.views.DelegationTypesView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

/**
 * The DelegationTypesEndpoint allows managing delegation types.
 * Delegation types can be created, deleted, listed, or updated.
 *
 */
@Api(authorizations = @Authorization("client-auth"))
@Path("/delegation_types")
public class DelegationTypesEndpoint extends ApiEndpoint {

    @Inject
    private DelegationTypeService delegationTypeService;

    /**
     * List delegation types.
     * 
     */
    @ApiOperation(
            value = "Gets delegation types",
            tags = {"info", "delegation_types"})
    @GET
    public Response list(@BeanParam final ListParams params) {
        if (params == null) {
            return Response.status(BAD_REQUEST).entity("Entity cannot be null").build();
        }

        DelegationTypesView view = delegationTypeService.findAll(params.getFilter());
        return Response.ok().entity(view).build();
    }

    /**
     * List delegation type for a given crn.
     * 
     */
    @ApiOperation(
            value = "Gets a delegation",
            tags = {"info", "delegation_types"})
    @GET
    @Path("{crn : .+}")
    public Response show(@PathParam("crn") final String crn) {

        if (crn == null) {
            throw new BadRequestException("crn must not be empty");
        }

        DelegationTypeView view = delegationTypeService.findByCrn(crn).orElseThrow(NotFoundException::new);
        return Response.ok().entity(view).build();

    }

    /**
     * Create a new delegation type.
     * 
     */
    @ApiOperation(
            value = "Creates a delegation type",
            tags = {"core", "delegation_types"})
    @POST
    public Response create(@Context final UriInfo ui, final DelegationType params) {
        if (params == null) {
            return Response.status(BAD_REQUEST).entity("Entity cannot be null").build();
        }

        ServiceResponse<DelegationType> sr = delegationTypeService.create(params);

        if (sr.successful()) {
            URI uri = ui.getRequestUriBuilder().path("{crn}").build(sr.asSuccess().getId());
            return Response.created(uri).build();
        } else {
            return Response.status(BAD_REQUEST).entity(sr.asError().message()).build();
        }
    }

    /**
     * Update an existing delegation type.
     * 
     */
    @ApiOperation(
            value = "Update a delegation type",
            tags = {"info", "delegation_types"})
    @PUT
    @Path("{crn : .+}")
    public Response update(@PathParam("crn") final String crn, final DelegationType params) {
        if (crn == null) {
            return Response.status(BAD_REQUEST).entity("Crn cannot be null").build();
        }

        if (params == null) {
            return Response.status(BAD_REQUEST).entity("Params cannot be null").build();
        }

        ServiceResponse<DelegationType> sr = delegationTypeService.update(crn, params);

        if (sr.successful()) {
            return Response.ok().build();
        } else {
            return Response.status(BAD_REQUEST).entity(sr.asError().message()).build();
        }
    }

    /**
     * Delete an old delegation type.
     * 
     */
    @ApiOperation(
            value = "Deletes a delegation type",
            tags = {"info", "delegation_types"})
    @DELETE
    @Path("{crn : .+}")
    public Response delete(@Context final UriInfo ui, @PathParam("crn") final String crn) {
        if (crn == null) {
            return Response.status(BAD_REQUEST).entity("Crn cannot be null").build();
        }

        ServiceResponse<DelegationType> sr = delegationTypeService.delete(crn);

        if (sr.successful()) {
            return Response.ok().build();
        } else {
            return Response.status(BAD_REQUEST).entity(sr.asError().message()).build();
        }
    }

}
