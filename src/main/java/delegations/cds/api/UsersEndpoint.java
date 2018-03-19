package delegations.cds.api;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import delegations.cds.services.UserService;
import delegations.cds.views.UsersView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

/**
 * Lists users.
 *
 */
@Api(authorizations = @Authorization("client-auth"))
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsersEndpoint {

    @Inject
    private UserService userService;

    /**
     * Lists users.
     * 
     */
    @ApiOperation(
            value = "Gets users",
            tags = {"admin", "users"})
    @GET
    public Response list(@BeanParam final ListParams params) {
        if (params == null) {
            return Response.status(BAD_REQUEST).entity("Entity cannot be null").build();
        }

        UsersView view = userService.findAll(params.getFilter());


        return Response.ok().entity(view).build();
    }
}
