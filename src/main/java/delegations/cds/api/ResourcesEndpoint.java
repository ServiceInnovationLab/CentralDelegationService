package delegations.cds.api;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.net.URI;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import delegations.cds.models.Resource;
import delegations.cds.services.ResourceService;
import delegations.cds.services.ServiceResponse;
import delegations.cds.views.ResourceAccessView;
import delegations.cds.views.ResourceView;
import delegations.cds.views.ResourcesView;
import io.opentracing.contrib.jaxrs2.server.Traced;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ResponseHeader;

/**
 * The ResourcesEndpoint allows managing of resources.
 * 
 * A resource is the data or service an organisation holds for a user that the user wants to delegate access to someone else for.
 * Delegations holds an id for a resource (a crn) that the organisation can use to query Delegations about whether
 * or not someone should have access to it.
 * 
 * Resources can be created, deleted and listed. The 'access' method also allows an organisation to query whether a given user
 * has delegated permission to access a particular resource.
 *
 */
@Api(authorizations = @Authorization("client-auth"))
@Path("/resources")
public class ResourcesEndpoint extends ApiEndpoint {

    @Inject
    private ResourceService resourceService;

    /**
     * Creates a new resource.
     * 
     */
    @ApiOperation(
            value = "Creates a resource",
            tags = {"core", "resources"})
    @POST
    public Response create(@Context final UriInfo ui, final Resource params) {
        logger.info("resources::create");
        if (params == null) {
            return Response.status(BAD_REQUEST).entity("Entity cannot be null").build();
        }

        ServiceResponse<Resource> sr = resourceService.create(params);

        if (sr.successful()) {
            URI uri = ui.getRequestUriBuilder().path("{crn}").build(sr.asSuccess().getId());
            return Response.created(uri).build();
        } else {
            return Response.status(BAD_REQUEST).entity(sr.asError().message()).build();
        }
    }

    /**
     * Deletes an existing resource.
     * 
     */
    @ApiOperation(
            value = "Deletes a resource",
            tags = {"info", "resources"})
    @DELETE
    @Path("{crn : .+}")
    public Response delete(@Context final UriInfo ui, @PathParam("crn") final String crn) {

        if (crn == null) {
            return Response.status(BAD_REQUEST).entity("Resource:Crn cannot be null").build();
        }


        ServiceResponse<Resource> sr = resourceService.delete(crn);

        if (sr.successful()) {
            return Response.ok().build();
        } else {
            return Response.status(BAD_REQUEST).entity(sr.asError().message()).build();
        }
    }



    /**
     * Lists resources.
     * 
     */
    @ApiOperation(
            value = "Gets resources",
            tags = {"info", "resources"})
    @GET
    @Traced(operationName = "api/resources/list")
    public Response list(@BeanParam final ListParams params) {

        if (params == null) {
            return Response.status(BAD_REQUEST).entity("Entity cannot be null").build();
        }

        ServiceResponse<ResourcesView> sr = resourceService.findAll(params.getFilter());


        if (sr.successful()) {
            ResourcesView view = sr.asSuccess().payload();
            return Response.ok().entity(view).build();
        } else {
            return Response.status(BAD_REQUEST).entity(sr.asError().message()).build();
        }

    }

    /**
     * Shows details for a resource given a particular crn.
     * 
     */
    @ApiOperation(
            value = "Gets a resource",
            tags = {"info", "resources"})
    @GET
    @Path("{crn : .+}")
    public Response show(@PathParam("crn") final String crn) {

        logger.info("resources::show");

        if (crn == null) {
            throw new BadRequestException("crn must not be empty");
        }

        try {
            ResourceView resource = resourceService.findByCrn(crn);
            return Response.ok().entity(resource).build();
        } catch (NoResultException e) {
            throw new NotFoundException(e);
        }

    }

    /**
     * Checks whether a given user (identified by an rcms token) has permission to access
     * a given resource (crn).
     * 
     */
    @ApiOperation(
            value = "Checks access to a resource",
            code = 204,
            response = Void.class,
            tags = {"core", "resources"})
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "Success. Access is allowed."),
            @ApiResponse(
                    code = 401,
                    message = "Uanauthorized. Authentication of the calling client has failed.",
                    responseHeaders = @ResponseHeader(
                            name = HttpHeaders.WWW_AUTHENTICATE,
                            description = "Challenge for Basic Auth",
                            response = String.class)),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden. Access is not allowed.")

    })
    @GET
    @Path("{crn : .+}/access/{rcms}")
    public Response access(@PathParam("crn") final String crn, @PathParam("rcms") final String requestorRcms) {

        logger.info("resources::access");

        if (crn == null) {
            throw new BadRequestException("crn must not be empty");
        }
        ServiceResponse<ResourceAccessView> accessResponse = resourceService.checkAccess(crn, requestorRcms);
        if (!accessResponse.successful()) {
            if (accessResponse.asError().exception() instanceof WebApplicationException) {
                throw (WebApplicationException) accessResponse.asError().exception();
            } else {
                throw new InternalServerErrorException(accessResponse.asError().exception());
            }
        }
        ResourceAccessView view = accessResponse.asSuccess().payload();

        return Response.ok().entity(view).build();

    }

}
